# Makefiles: General Reference

## What a Makefile does

`make` is a build tool that tracks **what needs to be rebuilt** based on file modification times. It reads `Makefile` in the current directory, finds the target you asked for (or the first target by default), checks if its dependencies are up to date, and runs the recipe only if something changed.

---

## Anatomy of a rule

```makefile
target: dependency1 dependency2
	recipe command
```

- **target** — the file to produce, or a name for a task
- **dependencies** — files the target depends on; if any are newer than the target, the recipe runs
- **recipe** — shell commands to run (MUST be indented with a real tab, not spaces)

Example:
```makefile
main.o: main.c utils.h
	gcc -c main.c
```
This says: if `main.c` or `utils.h` is newer than `main.o`, recompile.

---

## Variables

```makefile
CC     = gcc
CFLAGS = -Wall -g

# use a variable with $(NAME)
main.o: main.c
	$(CC) $(CFLAGS) -c main.c
```

### `:=` vs `=`
| Assignment | Behavior |
|------------|----------|
| `VAR = value` | Lazily evaluated — expanded each time `$(VAR)` is used |
| `VAR := value` | Immediately evaluated — expanded once when the line is read |

Prefer `:=` for variables derived from functions like `$(wildcard ...)` so they aren't re-evaluated on every use.

---

## Automatic variables

Inside a recipe, these expand to parts of the rule automatically:

| Variable | Expands to |
|----------|-----------|
| `$@` | The target name |
| `$<` | The first dependency |
| `$^` | All dependencies (space-separated) |
| `$*` | The `%` stem in a pattern rule |

Example:
```makefile
vec.o: vec.c vec.h
	gcc -c -o $@ $<
# expands to: gcc -c -o vec.o vec.c
```

---

## Pattern rules

Instead of writing one rule per file, write one rule with `%` as a wildcard stem:

```makefile
%.o: %.c
	$(CC) $(CFLAGS) -c $<
```

This matches any `.o` target and compiles the corresponding `.c`. The `%` stem is the same on both sides — `vec.o` matches `vec.c`, `list.o` matches `list.c`, etc.

---

## Functions

### `$(wildcard pattern)`
Returns a space-separated list of files matching the pattern:
```makefile
SRCS := $(wildcard *.c)       # all .c files in the directory
TESTS := $(wildcard *_test.c) # only test files
```

### `$(patsubst pattern, replacement, text)`
Replaces parts of a list of words:
```makefile
OBJS := $(patsubst %.c, %.o, $(SRCS))  # swap .c for .o in every name
```

### Shorthand substitution reference
```makefile
OBJS := $(SRCS:.c=.o)  # same as patsubst above, shorter syntax
```

---

## .PHONY targets

A phony target is a name for a task, not a real file. Without `.PHONY`, if a file with that name exists, `make` skips the recipe thinking it's already up to date.

```makefile
.PHONY: all test clean

test: $(TEST_BINS)
	./run_tests.sh

clean:
	rm -f *.o *.d $(TEST_BINS)
```

Common phony targets: `all`, `test`, `clean`, `install`, `run`.

---

## Suppressing output

Prefix a recipe line with `@` to stop `make` from printing the command before running it:
```makefile
test: $(TEST_BINS)
	@echo "Running tests..."   # prints the message but not the echo command itself
	@./my_test
```

---

## Shell variables inside recipes

Each recipe line runs in its own shell. To use a shell variable (`$var`) inside a `make` recipe, double the `$` so `make` doesn't consume it:

```makefile
test: $(TEST_BINS)
	@for t in $(TEST_BINS); do echo "--- $$t ---"; ./$$t; done
#                                       ^^^                ^^^
#                          $$t means $t in the shell, not a make variable
```

---

## Auto-generated header dependencies

When a `.c` file changes, `make` recompiles it. But if only a `.h` file changes, `make` won't know unless you list it as a dependency. Doing that manually is error-prone.

Add `-MMD -MP` to `CFLAGS` and the compiler will write a `.d` file alongside each `.o` with the exact headers that file used. Then include those files in the Makefile:

```makefile
CFLAGS = -Wall -g -MMD -MP

-include $(wildcard *.d)
```

The `-include` (with a dash) silently skips missing `.d` files on the first build when they don't exist yet.

---

## A minimal but scalable C project Makefile

```makefile
CC      = gcc
CFLAGS  = -Wall -Wextra -g -fsanitize=address -MMD -MP

# Discover all sources and test files automatically
SRCS      := $(wildcard *.c)
TEST_SRCS := $(wildcard *_test.c)
TEST_BINS := $(TEST_SRCS:.c=)

.PHONY: test clean

# Build and run all test binaries
test: $(TEST_BINS)
	@for t in $(TEST_BINS); do echo "--- $$t ---"; ./$$t; done

# Pattern rule: link each test binary
%_test: %_test.o
	$(CC) $(CFLAGS) -o $@ $^

# Pattern rule: compile any .c to .o
%.o: %.c
	$(CC) $(CFLAGS) -c $<

# Include auto-generated header dependencies
-include $(wildcard *.d)

clean:
	rm -f *.o *.d $(TEST_BINS)
```

---

## Common compiler flags reference

| Flag | Purpose |
|------|---------|
| `-Wall` | Enable common warnings |
| `-Wextra` | Enable extra warnings beyond `-Wall` |
| `-g` | Include debug symbols (needed for readable stack traces) |
| `-O2` | Optimize for speed (use in release builds, not debug) |
| `-fsanitize=address` | AddressSanitizer: catches memory bugs at runtime |
| `-fsanitize=undefined` | UBSan: catches undefined behavior (integer overflow, bad casts, etc.) |
| `-c` | Compile only, do not link (produces `.o`) |
| `-o name` | Set the output file name |
| `-MMD -MP` | Auto-generate header dependency files (`.d`) |

---

## Order of operations when `make test` runs

```
make test
  → needs $(TEST_BINS) built first
      → each test binary needs its .o file(s)
          → each .o is compiled from its .c (%.o: %.c rule)
      → binary is linked from .o files (%_test: %_test.o rule)
  → shell loop runs each binary
```

`make` resolves this dependency tree automatically — you only describe the relationships, not the order.
