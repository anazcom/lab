# 5-Day C Learning Curriculum

Project-based C for experienced programmers. Each day is ~4–6 hours. You already know how to code — this focuses on what makes C *different*: manual memory, the machine model, and POSIX systems APIs.

## Prerequisites

```bash
# macOS
brew install gcc valgrind lldb

# Or use clang (ships with Xcode CLT)
xcode-select --install
```

## Structure

```
day1/   Compilation pipeline, pointers, pointer arithmetic  →  cat clone
day2/   Stack vs heap, malloc/free, Valgrind                →  dynamic array (vector)
day3/   File descriptors, fork/exec/wait, pipes             →  mini shell
day4/   Strings, linked lists, hash maps                    →  key-value store CLI
day5/   BSD sockets, HTTP/1.1                               →  static file HTTP server
```

## How to use this repo

Each `dayN/` folder has:
- `README.md` — concepts + project spec
- `*.c` / `*.h` starter files with `TODO` markers
- `Makefile` — run `make` to build, `make clean` to reset

Start each day by reading the README, then work through the TODOs. The starter files compile clean (they just don't *do* anything yet).

## The C mental model you need to build

Unlike Python/JS/Java, C gives you:
1. **No runtime** — what you write is almost literally what the CPU executes
2. **Manual memory** — nothing is freed unless you free it
3. **No bounds checking** — read past an array and you get undefined behavior
4. **Pointers are addresses** — an `int *p` is just a 64-bit number holding a memory address

That's it. The whole language is a thin veneer over the machine.
