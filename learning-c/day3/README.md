# Day 3: Processes, File Descriptors, and Pipes

## What you'll learn

### File descriptors

In Unix, everything is a file. A **file descriptor** (fd) is just a small integer that the kernel gives you as a handle to any I/O resource: a file, a pipe, a socket, a terminal.

Every process starts with three open:
- `0` — stdin
- `1` — stdout
- `2` — stderr

```c
#include <unistd.h>
#include <fcntl.h>

int fd = open("file.txt", O_RDONLY);   // returns a new fd (e.g., 3)
read(fd, buffer, nbytes);
write(fd, buffer, nbytes);
close(fd);
```

`read` and `write` operate on raw bytes. `fopen`/`fgets` from `<stdio.h>` are buffered wrappers built on top of these.

### How a shell works — the mental model

A shell is a **REPL** (Read-Eval-Print Loop) that:
1. **Reads** a line of text from the user
2. **Parses** it into a command + arguments
3. **Executes** it by spawning a child process
4. **Waits** for the child to finish
5. Loops back to 1

That's the whole thing. The complexity comes from extras: pipes, redirections, background jobs, built-in commands.

### fork and exec — how Unix runs programs

Unix uses a two-step model to run programs, which is unusual and important to understand:

**`fork()`** — creates an exact copy of the current process. Both the parent and child continue executing from the same point. The only difference: `fork()` returns `0` in the child and the child's PID in the parent.

**`exec()`** — replaces the current process's code and memory with a new program. The process ID stays the same, but everything else changes. If `exec` succeeds, it never returns.

```
shell process (pid 100)
│
├── fork() ─────────────────────────────────────────────────────────┐
│                                                                    │
│  parent (pid 100)           child (pid 101)                       │
│  fork() returned 101        fork() returned 0                     │
│  waitpid(101, ...)          execvp("ls", args)                    │
│  (blocks until child exits) └─ ls is now running as pid 101       │
│                               ls exits with code 0                │
│  waitpid returns                                                   │
│  loop back to prompt                                               │
```

Key syscalls:
```c
#include <unistd.h>
#include <sys/wait.h>

pid_t fork(void);
int   execvp(const char *file, char *const argv[]);  // search PATH
pid_t waitpid(pid_t pid, int *status, int options);
```

### Pipes

A pipe is a unidirectional byte channel between two processes. `pipe(fds)` gives you two file descriptors: `fds[0]` for reading, `fds[1]` for writing. What you write into `fds[1]` comes out of `fds[0]`.

```
ls | grep .c
```

How the shell implements this:
1. Create a pipe: `pipe(fds)`
2. Fork twice (one child per command)
3. In child 1 (`ls`): close `fds[0]`, replace stdout with `fds[1]` using `dup2`, exec `ls`
4. In child 2 (`grep`): close `fds[1]`, replace stdin with `fds[0]` using `dup2`, exec `grep`
5. Parent closes both ends and waits for both children

`dup2(oldfd, newfd)` is the key: it makes `newfd` refer to the same underlying resource as `oldfd`. After `dup2(fds[1], STDOUT_FILENO)`, writing to stdout writes to the pipe.

### Built-in commands

Some commands CANNOT be implemented as child processes because they need to modify the shell's own state:
- `cd` — changes the shell's working directory (a child's `chdir` won't affect the parent)
- `exit` — exits the shell
- `export` — sets environment variables in the shell

These must be handled before `fork`, directly in the shell process.

---

## Project: Mini Shell

Build a working Unix shell. Start minimal and add features incrementally.

### Phase 1 — The REPL (get this working first)

```
$ ./mysh
mysh> ls
(output of ls)
mysh> pwd
/home/you/learning-c/day3
mysh> exit
```

Your shell should:
- Print a prompt
- Read a line with `fgets` or `readline`
- Split it into tokens (command + arguments)
- Fork, exec, wait
- Loop

### Phase 2 — Built-ins

Implement `cd`, `exit`, and optionally `pwd` as built-ins (don't fork for these).

### Phase 3 — Pipes

Support a single pipe:
```
mysh> ls | grep .c
```

Parse the line, detect the `|`, split into two commands, set up the pipe, fork twice.

### Stretch: Phase 4 — Redirections

```
mysh> ls > out.txt
mysh> cat < in.txt
mysh> ls >> log.txt
```

Use `open` + `dup2` to redirect stdin/stdout to files.

---

## How to think about building this

**Step 1: Don't think about the shell yet. Understand fork+exec first.**

Write a tiny program (not the shell) that just forks and execs `ls`. Get comfortable with the return value of `fork()` and the fact that `exec` never returns on success. Print the PID from both parent and child so you can see what's happening.

**Step 2: Build the REPL loop in isolation.**

A prompt loop that reads lines and prints them back is 10 lines of C. Get that right before adding execution. Focus on: how do you read a line? How do you handle EOF (Ctrl+D)?

**Step 3: Parse the command line.**

The simplest parser: use `strtok(line, " \t\n")` to split by whitespace into tokens. Store them in a `char *argv[]` array with a `NULL` sentinel at the end. `execvp` takes exactly this format.

**Step 4: Wire fork+exec into the loop.**

Now combine: read a line, parse it, fork, exec in the child, wait in the parent.

**Step 5: Add built-ins before forking.**

Check if `argv[0]` is `"cd"` or `"exit"` before you fork. Handle them directly.

**Step 6: Add pipes.**

Scan the token list for `"|"`. Split at that token. Set up `pipe()`, fork twice, use `dup2` to rewire stdin/stdout in each child.

### Common mistakes

- Forgetting to close unused pipe ends in the parent (causes children to hang waiting for EOF)
- Not setting `argv[last+1] = NULL` — `execvp` requires a NULL-terminated array
- Calling `exit()` in the parent's branch of the fork instead of the child's
- Not handling `execvp` failure (it returns -1 if the command isn't found — print an error)

### Testing your shell

The shell is harder to unit test than the vector because its logic is tightly coupled to the OS (forking, exec, pipes). The practical approach is a mix of unit tests for the parseable parts and manual/scripted integration tests for the rest.

**Unit test the parser with `test.h` from Day 2:**
The tokenizer (splitting a line into `argv`) is pure logic with no syscalls — test it in isolation. Copy `test.h` into `day3/`. Write a `parse_test.c` that calls your tokenizer and uses `CHECK` to verify:
- `"ls -la"` → `["ls", "-la", NULL]`
- `"echo hello world"` → `["echo", "hello", "world", NULL]`
- Empty input → handled gracefully (no crash)
- `"ls | grep .c"` → correct split at `|`

**Integration test the shell behavior with a script:**
Write a `test.sh` that runs your shell non-interactively and checks its output:
```bash
echo "echo hello" | ./mysh | grep -q "hello" && echo PASS || echo FAIL
echo "cd /tmp && pwd" | ./mysh | grep -q "/tmp" && echo PASS || echo FAIL
```
Add a `make test` target that runs both `parse_test` and `test.sh`.

**What to verify manually:**
- Ctrl+D (EOF) exits cleanly, no crash
- Unknown command prints an error but doesn't exit the shell
- Pipe output matches what you'd get running the commands directly in bash

### Suggested reading (man pages)

```bash
man 2 fork
man 2 execvp
man 2 waitpid
man 2 pipe
man 2 dup2
man 3 strtok
```
