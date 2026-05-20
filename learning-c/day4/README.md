# Day 4: Strings, Linked Lists, and Hash Maps

## What you'll learn

### C strings, revisited

You've seen that C strings are null-terminated `char` arrays. Today you'll work with them heavily, so here are the important standard functions:

```c
#include <string.h>

strlen(s)              // length, not counting '\0'
strcpy(dst, src)       // copy src into dst (dst must be large enough!)
strncpy(dst, src, n)   // safer: copy at most n bytes
strcat(dst, src)       // append src to dst
strcmp(a, b)           // 0 if equal, <0 if a<b, >0 if a>b
strdup(s)              // malloc + strcpy in one (you must free the result)
strtok(s, delim)       // tokenize (modifies the string in place — watch out)
```

The key danger: C never checks if your destination buffer is large enough. `strcpy` into a buffer that's too small will silently corrupt memory. Prefer `snprintf` over `sprintf` for the same reason.

### Linked lists

A linked list is the canonical C data structure because it's built from pointers. Each node holds a value and a pointer to the next node:

```
head
 │
 ▼
[key="foo", val="bar", next]──→[key="baz", val="qux", next]──→NULL
```

```c
typedef struct Node {
    char        *key;
    char        *value;
    struct Node *next;   // pointer to the same struct type — must use "struct Node" here
} Node;
```

Operations:
- **Prepend**: create a new node, point its `next` at current head, update head. O(1).
- **Search**: walk the list comparing keys. O(n).
- **Delete**: walk until you find the node, update the previous node's `next` to skip it, free the node. O(n).

### Hash maps

A hash map gives you O(1) average-case lookup by combining hashing with an array of linked lists (separate chaining):

```
index:  0    1    2    3    4    5    6    7
        │         │              │
        ▼         ▼              ▼
      [a=1]     [b=2]          [c=3]──→[d=4]──→NULL
```

The algorithm:
1. **Hash** the key to get an integer
2. **Mod** by the number of buckets to get an index
3. **Search** the linked list at that index for the key

A simple but effective hash function (djb2):
```
hash = 5381
for each character c in key:
    hash = hash * 33 + c
```

Choosing the number of buckets: a prime number reduces collisions. 64 or 256 is fine for a small map.

### String parsing for a CLI

Your key-value store will read commands like:
```
set name Alice
get name
delete name
list
quit
```

Parse this by reading a line, then using `strtok` or manual scanning to extract the verb and arguments. Use `strcmp` to dispatch to the right handler.

---

## Project: Key-Value Store CLI

Build an in-memory key-value store with a command-line interface — like a tiny Redis.

### Spec

```
$ ./kv
kv> set name Alice
OK
kv> set age 30
OK
kv> get name
Alice
kv> get missing
(nil)
kv> list
age = 30
name = Alice
kv> delete name
OK
kv> get name
(nil)
kv> quit
```

### Data model

- Keys and values are both strings
- The store is backed by a hash map
- Each bucket in the hash map is a linked list of `(key, value)` pairs

### How to think about building this

**Start with the linked list, not the hash map.**

Build and test a simple linked list that stores key-value pairs. Implement `insert`, `lookup`, and `delete` on just the list. Print it to verify it works. This is your hash bucket — everything else builds on it.

**Then build the hash map as a fixed array of list heads.**

Declare `Node *buckets[NUM_BUCKETS]` (initialized to all NULL). Implement `hash(key) % NUM_BUCKETS` to pick the right bucket. Now `set`, `get`, and `delete` are just list operations on `buckets[hash(key) % NUM_BUCKETS]`.

**Then build the REPL.**

Read a line, parse the command, call the right hash map function. Same pattern as the shell's REPL from Day 3.

**Memory ownership is the hard part.**

When you `set name Alice`:
- You need to store copies of `"name"` and `"Alice"` in the node — not pointers to the input buffer (which will be overwritten on the next read).
- Use `strdup` to copy them.
- When you `delete` a key or overwrite it with a new `set`, you must `free` the old key and value strings, then `free` the node itself.
- When the program exits, walk every bucket and free every node.

Draw a picture of a node in memory and what needs to be freed when it's deleted. There are 3 things.

### Testing your key-value store

The hash map and linked list are pure data structure logic — perfect for unit testing with the runner from Day 2. Copy `test.h` into `day4/` and create `kv_test.c`.

**Unit test the hash map directly (bypass the REPL):**
Your `set`, `get`, and `delete` functions should be callable from test code without going through the command parser. If you design them as standalone functions that take a map pointer, testing them is straightforward.

Cases worth covering:
- `set` then `get` returns the right value
- `get` on a missing key returns NULL (not a crash)
- `set` the same key twice — `get` returns the new value, old value is freed
- `delete` then `get` returns NULL
- `delete` a key that doesn't exist — no crash
- Hash collisions: insert two keys that hash to the same bucket, verify both are retrievable
- Insert 1000 keys, retrieve all of them correctly

**Test the hash function itself:**
Your hash function should distribute keys across buckets reasonably. Write a test that inserts 100 keys and checks that no single bucket has more than ~10 entries (a very uneven distribution means a bad hash or a bug).

**Test memory with AddressSanitizer:**
Run `make test` with `-fsanitize=address`. Every `set`, `delete`, and program exit path must be leak-free. ASan will catch use-after-free on deleted nodes.

```makefile
kv_test: kv.c kv_test.c
	$(CC) $(CFLAGS) kv.c kv_test.c -o kv_test

test: kv_test
	./kv_test
```

### Stretch goals
- Support integer values with a separate `incr` command
- Persist the store to a file on exit and reload it on startup
- Implement `keys` to list only keys, `values` to list only values
- Resize the hash map when the load factor exceeds a threshold
