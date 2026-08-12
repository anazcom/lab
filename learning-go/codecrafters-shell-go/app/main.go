package main

import (
	"bufio"
	"errors"
	"fmt"
	"io"
	"io/fs"
	"log"
	"os"
	"os/exec"
	"path/filepath"
	"slices"
	"strings"
)

func main() {
	log.SetOutput(io.Discard)
	reader := bufio.NewReader(os.Stdin)
	pathDirs := strings.Split(os.Getenv("PATH"), ":")
	for {
		fmt.Print("$ ")
		args, err := readInput(reader)
		if err != nil {
			fmt.Fprintf(os.Stderr, "Something went wrong while reading %s\n", err)
			continue
		}
		if len(args) == 0 {
			continue
		}

		name, args := args[0], args[1:]

		switch name {
		case "exit":
			os.Exit(0)
		case "echo":
			fmt.Println(strings.Join(args, " "))
		case "cd":
			if len(args) > 1 {
				fmt.Fprintln(os.Stderr, "cd: Expected only one argument")
				continue
			}
			arg := args[0]
			if arg == "~" {
				arg = os.Getenv("HOME")
				if arg == "" {
					fmt.Fprintln(os.Stderr, "cd: HOME environment is not setup")
					continue
				}
			}
			path, err := filepath.Abs(arg)
			if err != nil {
				fmt.Fprintf(os.Stderr, "cd: %s: No such file or directory\n", path)
				continue
			}
			info, err := os.Stat(arg)
			if err != nil {
				fmt.Fprintf(os.Stderr, "cd: %s: No such file or directory\n", path)
				continue
			}
			if !info.IsDir() {
				path = filepath.Dir(path)
			}
			os.Chdir(path)
		case "pwd":
			cwd, err := os.Getwd()
			if err != nil {
				fmt.Fprintln(os.Stderr, "pwd: error getting working directory")
				continue
			}
			fmt.Println(cwd)
		case "type":
			for _, arg := range args {
				arg = strings.TrimSpace(arg)
				if isBuiltin(arg) {
					fmt.Println(arg + " is a shell builtin")
				} else if path, err := resolvePathFor(arg, pathDirs); err == nil {
					fmt.Printf("%s is %s\n", arg, path)
				} else {
					fmt.Println(arg + ": not found")
				}
			}
		default:
			if _, err := resolvePathFor(name, pathDirs); err != nil {
				fmt.Println(name + ": not found")
				continue
			}

			cmd := exec.Command(name, args...)
			cmd.Stdin = os.Stdin
			cmd.Stdout = os.Stdout

			err := cmd.Run()
			if err != nil {
				fmt.Fprintf(os.Stderr, "ERROR: An error occured when running %s, %s\n", name, err)
				continue
			}
		}
	}
}

var builtins = []string{"exit", "type", "echo", "pwd", "cd"}

func isBuiltin(command string) bool {
	return slices.Contains(builtins, command)
}

func isExecutable(mode fs.FileMode) bool {
	return (mode.Perm() & 0o111) != 0
}

func resolvePathFor(command string, dirs []string) (string, error) {
	for _, dir := range dirs {

		fullpath := filepath.Join(dir, command)
		info, err := os.Stat(fullpath)
		if err != nil {
			continue // not found on dir folder
		}

		if isExecutable(info.Mode()) {
			return fullpath, nil
		}
	}
	return "", errors.New("Command was not found")
}

type State int

const (
	ReadingSingleQuote State = iota
	ReadingText
	ReadingTextEscape
	ReadingDoubleQuote
	ReadingDoubleQuoteEscape
)

func readInput(reader *bufio.Reader) ([]string, error) {
	var ret []string
	var current strings.Builder

	line, err := reader.ReadString('\n')
	if err == io.EOF {
		os.Exit(0) // stdin closed, exit cleanly
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, "Error: something went wrong while reading")
		os.Exit(1)
	}

	line = strings.TrimRight(line, "\r\n")
	log.Printf("readInput: line=%q", line)
	if line == "" {
		return ret, nil
	}

	state := ReadingText
	istart := 0

	for icur := 0; icur < len(line); icur++ {
		c := line[icur]
		log.Printf(" istart=%d icur=%d c=%q state=%v", istart, icur, c, state)
		switch state {
		case ReadingSingleQuote:
			if c == '\'' {
				current.WriteString(line[istart:icur])
				istart = icur + 1
				state = ReadingText
			}
		case ReadingDoubleQuote:
			if c == '"' {
				current.WriteString(line[istart:icur])
				istart = icur + 1
				state = ReadingText
			}
			if c == '\\' {
				if istart < icur {
					current.WriteString(line[istart:icur])
				}
				istart = icur + 1
				state = ReadingDoubleQuoteEscape
			}
		case ReadingDoubleQuoteEscape:
			state = ReadingDoubleQuote
		case ReadingText:
			if c == '"' {
				current.WriteString(line[istart:icur])
				istart = icur + 1
				state = ReadingDoubleQuote
				continue
			}
			if c == '\'' {
				current.WriteString(line[istart:icur])
				istart = icur + 1
				state = ReadingSingleQuote
				continue
			}
			if c == '\\' {
				if istart < icur {
					current.WriteString(line[istart:icur])
				}
				istart = icur + 1
				state = ReadingTextEscape
			}
			if c == ' ' {
				if icur > istart {
					current.WriteString(line[istart:icur])
				}
				if current.Len() > 0 {
					ret = append(ret, current.String())
					current.Reset()
					log.Printf("resetting ------------")
				}
				istart = icur + 1
				continue
			}
		case ReadingTextEscape:
			state = ReadingText
		default:
			return nil, errors.New("Not implemented state")
		}
	}
	log.Printf(" istart=%d state=%v current=%s", istart, state, current.String())
	if state == ReadingSingleQuote {
		return nil, errors.New("Parsing error: Expected closing quote")
	}
	if istart < len(line) || current.Len() > 0 {
		current.WriteString(line[istart:])
		ret = append(ret, current.String())
	}
	return ret, nil
}
