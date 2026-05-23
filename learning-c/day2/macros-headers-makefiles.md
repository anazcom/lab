# C Concepts: Macros, Header Files, and Makefiles

## Header Files

Think of a `.h` file as a **shared declaration file** — like a TypeScript `.d.ts` or a Python module that other files import. It holds things that multiple `.c` files need to agree on.

A `.c` file is a **compilation unit** — the compiler sees it in isolation. If `vec_test.c` calls `vec_push()`, the compiler needs to have seen a declaration of `vec_push` before it compiles that line. The `.h` file is how you share those declarations.

```
test.h          ← macros and counters (shared across any test file)
vec.h           ← struct definition + function prototypes
vec.c           ← function implementations (includes vec.h)
vec_test.c      ← tests (includes vec.h and test.h)
```

`#include "test.h"` is literally a **text paste** — the preprocessor replaces that line with the full contents of `test.h` before the compiler sees anything. That's it. No magic.

**The include guard** is why you wrap header content in:
```c
#ifndef TEST_H
#define TEST_H

// ... your header content ...

#endif
```
Without it, if two files both include `test.h`, the compiler sees those declarations twice and errors. The guard makes the second paste a no-op.

---

## Macros

Macros are **text substitution** rules. The preprocessor runs before the compiler, and it does pure find-and-replace on your source text.

### Basic substitution
```c
#define MAX_SIZE 100
int arr[MAX_SIZE];   // becomes: int arr[100];
```

### Function-like macros
```c
#define SQUARE(x) x * x
SQUARE(3)    // becomes: 3 * 3
SQUARE(1+2)  // becomes: 1+2 * 1+2  ← BUG: 1+4+2 = 7, not 9
```
Always parenthesize parameters:
```c
#define SQUARE(x) ((x) * (x))
SQUARE(1+2)  // becomes: ((1+2) * (1+2))  ← correct
```

### The `#` operator (stringification)
`#` in front of a macro parameter turns the raw text of the argument into a string literal:
```c
#define SHOW(x) printf(#x " = %d\n", x)
SHOW(2 + 2);  // printf("2 + 2" " = %d\n", 2+2)
              // output: 2 + 2 = 4
```
The compiler concatenates adjacent string literals, so `"2 + 2" " = %d\n"` becomes `"2 + 2 = %d\n"`. This is how `CHECK(1 + 1 == 2)` can print `"1 + 1 == 2"` in the failure message — the preprocessor captures the expression as text before the compiler evaluates it.

### `__FILE__` and `__LINE__`
These are built-in macros the compiler fills in at each source location:
```c
printf("%s:%d\n", __FILE__, __LINE__);
// output: vec_test.c:42
```
Combine with `#` stringification and you get test framework failure messages for free.

### `do { } while (0)` — why it exists
When a macro expands to multiple statements, it can break `if/else` without braces:
```c
#define BAD(x)  _tests_run++; if (!(x)) _tests_failed++

if (condition)
    BAD(1 + 1 == 3);   // expands to:
// if (condition)
//     _tests_run++;         ← only this is inside the if
// if (!(1 + 1 == 3))        ← this always runs
//     _tests_failed++;
```
Wrapping in `do { } while (0)` makes the whole macro a single statement that works everywhere:
```c
#define GOOD(x) do { _tests_run++; if (!(x)) _tests_failed++; } while (0)

if (condition)
    GOOD(1 + 1 == 3);  // the do/while is a single statement — correct
```

---

## Makefile for Tests

A Makefile is a **dependency graph + recipe runner**. Each target has dependencies and a shell command. `make` only rebuilds what's out of date.

```makefile
CC      = gcc
CFLAGS  = -Wall -Wextra -g -fsanitize=address

# 'make test' compiles vec_test.c + vec.c, then runs the binary
.PHONY: test
test: vec_test
	./vec_test

# link the test binary from compiled object files
vec_test: vec_test.o vec.o
	$(CC) $(CFLAGS) -o $@ $^

# compile each .c to a .o
vec_test.o: vec_test.c vec.h test.h
	$(CC) $(CFLAGS) -c $<

vec.o: vec.c vec.h
	$(CC) $(CFLAGS) -c $<

.PHONY: clean
clean:
	rm -f *.o vec_test
```

### Makefile automatic variables
| Variable | Meaning |
|----------|---------|
| `$@` | The target name (`vec_test`) |
| `$^` | All dependencies (`vec_test.o vec.o`) |
| `$<` | First dependency (`vec_test.c`) |

### Compiler flags
| Flag | Purpose |
|------|---------|
| `-Wall -Wextra` | Enable most warnings — treat these as errors |
| `-g` | Include debug symbols (needed for readable ASan stack traces) |
| `-fsanitize=address` | AddressSanitizer: catches memory bugs at runtime, free to use in dev |

The dependency lines (e.g. `vec_test.o: vec_test.c vec.h test.h`) tell `make` to recompile if any of those files change. When you update `test.h`, every file that includes it gets recompiled automatically.

---

## The Full Flow

When you run `make test`:

1. **Preprocessor** pastes `test.h` and `vec.h` into each `.c` file
2. **Compiler** turns each `.c` into a `.o` object file
3. **Linker** combines the `.o` files into the `vec_test` binary
4. **Make** runs `./vec_test` — AddressSanitizer watches for memory bugs

Once you have `test.h` written and the Makefile in place, the development loop is:

```
write a function in vec.c
  → add a test_* function in vec_test.c
  → run make test
  → repeat
```

Don't batch everything and test at the end — that's how you end up debugging three things at once.
