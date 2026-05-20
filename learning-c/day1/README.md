# Day 1: The Toolchain, Types, and Pointers

## What you'll learn

### 1. The compilation pipeline

```
source.c  →  [preprocessor]  →  source.i
          →  [compiler]      →  source.s   (assembly)
          →  [assembler]     →  source.o   (object file)
          →  [linker]        →  ./program
```

```bash
gcc -E main.c          # stop after preprocessing
gcc -S main.c          # stop after assembly (produces main.s)
gcc -c main.c          # stop after compilation (produces main.o)
gcc main.c -o main     # full pipeline
```

Useful flags you'll use every day:
- `-Wall -Wextra` — enable warnings (treat these as errors)
- `-g` — include debug symbols (needed for lldb/gdb)
- `-O2` — optimization level 2 (for release builds)
- `-fsanitize=address` — AddressSanitizer: catches buffer overflows at runtime

### 2. Types and sizes

```c
char    c = 'A';        // 1 byte, -128..127 (or 0..255 unsigned)
int     i = 42;         // 4 bytes on most platforms
long    l = 42L;        // 8 bytes on 64-bit
size_t  n = 100;        // unsigned, right type for sizes/indices
float   f = 3.14f;
double  d = 3.14;

// Always use these for explicit sizes (include <stdint.h>):
int32_t  i32 = 0;
uint64_t u64 = 0;
```

### 3. Pointers — the core of C

A pointer is just a variable that holds a memory address.

```c
int x = 42;
int *p = &x;    // p holds the address of x

*p = 99;        // dereference: write through the pointer
printf("%d\n", x);  // prints 99

// Pointer arithmetic: advancing by sizeof(type)
int arr[5] = {10, 20, 30, 40, 50};
int *q = arr;   // arr decays to a pointer to its first element
printf("%d\n", *(q + 2));  // prints 30
```

Key rules:
- `&x` = "address of x"
- `*p` = "value at address p" (dereference)
- Pointer arithmetic moves by `sizeof(*p)` bytes, not by 1 byte
- A `NULL` pointer (address 0) is never valid to dereference

### 4. Arrays vs pointers

Arrays and pointers are related but NOT the same:
- `int arr[5]` — `arr` is an array; `sizeof(arr)` = 20 bytes
- `int *p = arr` — `p` is a pointer; `sizeof(p)` = 8 bytes (on 64-bit)
- Array names *decay* to a pointer to the first element in most contexts
- You cannot `sizeof` a decayed pointer and get the array size — this is a classic bug

### 5. Quick sanity checks with `assert`

Before you have a real test runner, use `assert()` from `<assert.h>` to verify assumptions inline:

```c
#include <assert.h>

assert(strlen("hello") == 5);   // aborts with file + line number on failure
assert(p != NULL);
```

It's blunt — it crashes the program on the first failure and tells you nothing about what passed. On Day 2 you'll build a proper test runner. For today, `assert` is enough.

### 6. Strings in C

C has no string type. A "string" is a `char` array terminated by `'\0'`:

```c
char s[] = "hello";     // {'h','e','l','l','o','\0'} — 6 bytes
char *t = "hello";      // pointer to a string literal (read-only!)

strlen(s)  // 5 (does NOT count the null terminator)
sizeof(s)  // 6 (includes the null terminator)
```

---

## Project: `mycat` — a `cat` clone

Build a program that reads files and prints them to stdout, like the Unix `cat` command.

### Spec

```bash
./mycat file1.txt              # print file1.txt to stdout
./mycat file1.txt file2.txt    # concatenate and print both
./mycat -n file1.txt           # prefix each line with its line number
./mycat                        # read from stdin if no files given
```

### Files

- `mycat.c` — implement this
- `Makefile` — already set up

### Hints

- Use `fopen`, `fgetc`/`fgets`/`fread`, `fclose` from `<stdio.h>`
- Handle the `-n` flag by parsing `argv` before opening files
- For stdin passthrough, use `stdin` as the FILE* (it's already open)
- `perror("mycat")` prints a human-readable error from `errno`

### Stretch goals

- `-e` flag: show `$` at end of each line
- Support `--` to stop flag parsing
- Handle binary files gracefully (don't mangle them)
