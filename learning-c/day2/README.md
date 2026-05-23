# Day 2: Memory Management

## What you'll learn

### The memory model

When your program runs, the OS gives it a virtual address space split into regions:

```
High addresses
┌──────────────┐
│    Stack     │  ← grows downward; local variables, function call frames
│      ↓       │
│              │
│      ↑       │
│    Heap      │  ← grows upward; malloc/free lives here
├──────────────┤
│ BSS / Data   │  ← global and static variables
├──────────────┤
│    Text      │  ← your compiled code (read-only)
│Low addresses │
├──────────────┤
```

Every function call pushes a **stack frame** (local vars, return address). When the function returns, the frame is popped — automatically. The heap is different: you request memory explicitly and it stays alive until you free it.

### The malloc family

```c
#include <stdlib.h>

void *malloc(size_t size);          // allocate `size` bytes, uninitialized
void *calloc(size_t n, size_t sz);  // allocate n*sz bytes, zero-initialized
void *realloc(void *ptr, size_t size); // resize an existing allocation
void  free(void *ptr);              // release memory back to the heap
```

Key facts:
- `malloc` returns `void *` — cast it to the type you need, or let C do it implicitly
- `malloc` returns `NULL` on failure — always check
- Every `malloc` must have exactly one matching `free`
- `free(NULL)` is safe and does nothing
- After `free(p)`, `p` is a **dangling pointer** — reading/writing through it is undefined behavior

### Classic bugs (you WILL hit these)

| Bug | What happens |
|-----|-------------|
| **Memory leak** | malloc without free; process eats memory until it dies |
| **Buffer overflow** | Write past the end of an allocation; corrupts adjacent memory |
| **Use-after-free** | Access memory after free(); reads garbage, writes corrupt heap |
| **Double-free** | Call free() twice on the same pointer; corrupts heap internals |
| **Stack overflow** | Deep recursion or huge local arrays exhaust the stack |

### Valgrind & AddressSanitizer

Two tools that catch memory bugs at runtime:

```bash
# AddressSanitizer (fast, built into clang/gcc, good for development)
gcc -fsanitize=address -g myprogram.c -o myprogram
./myprogram   # ASan will report errors with a stack trace

# Valgrind (slower, more thorough, works on existing binaries)
valgrind --leak-check=full ./myprogram
```

Use `-fsanitize=address` during development (it's fast). Use Valgrind when you need to find a subtle leak.

### sizeof and pointer arithmetic

```c
int *arr = malloc(10 * sizeof(int));  // always use sizeof, not magic numbers
arr[3] = 42;                          // same as *(arr + 3) = 42
```

`sizeof(int)` is 4 on most platforms. Never hardcode `4` — use `sizeof`.

### Structs

```c
typedef struct {
    int   size;
    int   capacity;
    int  *data;
} Vec;
```
Structs are laid out contiguously in memory. Members are accessed with `.` (direct) or `->` (through pointer). Padding may be inserted between members for alignment — don't assume sizeof(struct) == sum of member sizes.

### Testing in C — building a test runner

C has no built-in test framework, so you build one. This is worth doing from scratch because it teaches you two important C features: **macros** and the **preprocessor**.

The goal is a `CHECK(condition)` macro that:
- Records how many tests ran
- Prints a failure message (with file name and line number) when a condition is false
- Doesn't stop on the first failure

Here are the pieces you need to understand:

**The `#` operator — stringification**
Inside a macro, putting `#` before a parameter converts it to a string literal at compile time:
```c
#define SHOW(x) printf(#x " = %d\n", x)
SHOW(42);   // prints: 42 = 42
SHOW(1+1);  // prints: 1+1 = 2
```
This is how your `CHECK` macro can print the failing condition as text — the preprocessor turns the expression into a string before the compiler ever sees it.

**`__FILE__` and `__LINE__`**
These are predefined macros the compiler fills in automatically:
- `__FILE__` expands to the current source file name (a string literal)
- `__LINE__` expands to the current line number (an integer)

They're how test frameworks produce useful failure messages like `FAIL vec_test.c:42`.

**`do { ... } while (0)` — the safe multi-statement macro**
When a macro expands to multiple statements, wrapping them in `do { } while (0)` makes it behave like a single statement in all contexts (inside `if` without braces, for example). This is the standard C idiom — you'll see it everywhere.

```c
// Unsafe: breaks with if (cond) MACRO(); else ...
#define BAD(x) stmt1; stmt2

// Safe: always works as a single statement
#define GOOD(x) do { stmt1; stmt2; } while (0)
```

**Putting it together — your test runner**

The runner needs two counters (tests run, tests failed) and a macro that updates them. A minimal but complete design:

```c
static int _tests_run    = 0;
static int _tests_failed = 0;

#define CHECK(cond) do { \
    _tests_run++; \
    if (!(cond)) { \
        printf("FAIL  %s:%d  %s\n", __FILE__, __LINE__, #cond); \
        _tests_failed++; \
    } \
} while (0)

#define REPORT() do { \
    printf("\n%d/%d passed\n", _tests_run - _tests_failed, _tests_run); \
} while (0)
```

Group related checks into functions, call them from `main`, call `REPORT()` at the end, and return non-zero if any test failed (so `make test` fails in CI):

```c
void test_something(void) {
    CHECK(1 + 1 == 2);
    CHECK(strlen("hi") == 2);
}

int main(void) {
    test_something();
    REPORT();
    return _tests_failed > 0 ? 1 : 0;
}
```

**Where to put it**

Create a `test.h` header with the counters and macros. Include it in any `*_test.c` file. This way you reuse the same runner across all projects from Day 2 onward — copy `test.h` into each day's folder.

---

## Project: Dynamic Array (Vector)

Implement a generic-ish resizable array — the C equivalent of Python's `list` or Java's `ArrayList`.

### Spec

Your vector should support:
- `vec_new()` — create an empty vector with an initial capacity
- `vec_push(v, value)` — append a value; resize (double capacity) when full
- `vec_get(v, i)` — return the element at index i
- `vec_set(v, i, value)` — set element at index i
- `vec_len(v)` — return the current number of elements
- `vec_free(v)` — release all memory

For simplicity, store `int` values. Once it works, think about how you'd make it generic (hint: `void *` and element size).

### How to think about this

**Start with the data:**
What does a dynamic array need to track? At minimum: the actual data, how many elements are currently in it, and how much space is allocated. Capture those three things in a struct.

**The resize strategy:**
When the array is full and you push a new element, you can't just ask for one more slot — that would make every push O(n). The standard trick is to double the capacity. This gives amortized O(1) pushes. `realloc` is your friend here: it tries to extend the existing allocation in place, and if it can't, it allocates a new block and copies the data for you.

**Who owns the memory?**
Decide upfront: `vec_new` mallocs the struct itself AND the internal data buffer. `vec_free` must free both. Draw it out:

```
Vec (on heap)
├── size     = 3
├── capacity = 4
└── data ────────→ [ 10 | 20 | 30 | ?? ]  (on heap, separate allocation)
```

**First deliverable: `test.h`**
Before writing any vector code, create `test.h` with the `CHECK` and `REPORT` macros described above. Then write `vec_test.c` that includes it. This file is where all your `test_*` functions live. Run `make test` — it should compile and report 0/0 passed (nothing tested yet). Now you have a working harness before any logic exists.

**Then write the vector, test as you go:**
Add one function at a time. After each one, add a `test_*` function that exercises it and run `make test`. Don't batch everything up and test at the end — that's how you end up debugging three things at once.

Good cases to cover:
- Push one element, check length and value
- Push past initial capacity (forces a resize), check all values survived
- `vec_get` on index 0, last index, and middle
- `vec_free` runs clean under AddressSanitizer (no leak report)

### Stretch goals
- Make it generic: store `void *` elements (or take an element size and use `memcpy`)
- Add `vec_pop`, `vec_insert`, `vec_delete`
- Add bounds checking that prints a useful error instead of segfaulting
