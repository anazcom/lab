# Day 5: Sockets and HTTP — Build a Web Server

## What you'll learn

### The BSD socket API

Sockets are the Unix abstraction for network I/O. Like files, they're file descriptors — you `read`/`write` to them. The API has more setup ceremony, but the model is the same.

A TCP server has this lifecycle:

```
socket()    → create an endpoint (fd)
bind()      → assign it an address (IP + port)
listen()    → mark it as passive (ready to accept connections)
accept()    → block until a client connects, returns a NEW fd for that connection
read/write  → communicate with the client through the new fd
close()     → close the connection fd
            → loop back to accept()
```

```c
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

int server_fd = socket(AF_INET, SOCK_STREAM, 0);

struct sockaddr_in addr = {
    .sin_family      = AF_INET,
    .sin_port        = htons(8080),   // htons: host byte order → network byte order
    .sin_addr.s_addr = INADDR_ANY,    // bind to all interfaces
};

bind(server_fd, (struct sockaddr *)&addr, sizeof(addr));
listen(server_fd, 10);   // 10 = backlog (pending connection queue size)

int client_fd = accept(server_fd, NULL, NULL);
// now read/write to client_fd
```

### HTTP/1.1 — what you actually need to know

HTTP is a text protocol. A request looks like:

```
GET /index.html HTTP/1.1\r\n
Host: localhost:8080\r\n
User-Agent: curl/7.88\r\n
\r\n
```

A response looks like:

```
HTTP/1.1 200 OK\r\n
Content-Type: text/html\r\n
Content-Length: 42\r\n
\r\n
<html><body>Hello, world!</body></html>
```

The structure:
- **Request line**: `METHOD /path HTTP/version\r\n`
- **Headers**: `Key: Value\r\n` (zero or more)
- **Blank line**: `\r\n` (signals end of headers)
- **Body**: (optional, for POST/PUT)

For a static file server you only need to handle `GET` and you only need to parse the path from the request line.

### Serving a file over HTTP

The request tells you the path (e.g., `/index.html`). You:
1. Strip the leading `/` to get a relative filesystem path
2. `open` the file
3. Read its contents
4. Send the HTTP response headers (status line + Content-Type + Content-Length)
5. Send the file contents
6. Close the connection

The trickiest part is `Content-Length` — you need to know the file size before you start sending. Use `fstat` or `stat` to get it.

### Content-Type

Browsers use the `Content-Type` header to decide how to render a response. A few common ones:

| Extension | Content-Type |
|-----------|-------------|
| `.html`   | `text/html` |
| `.css`    | `text/css` |
| `.js`     | `application/javascript` |
| `.json`   | `application/json` |
| `.png`    | `image/png` |
| `.txt`    | `text/plain` |

Map from file extension to content type using a simple lookup table.

### Concurrency (a preview)

Your server will handle one connection at a time (sequential). A real server needs to handle many simultaneously. Two classic approaches in C:
- **fork per connection**: `fork()` after `accept()`, handle the request in the child
- **threads**: `pthread_create()` per connection
- **non-blocking I/O + event loop**: `select()`/`poll()`/`epoll()` (this is what nginx, libuv, etc. do)

For the stretch goal, try the fork-per-connection model — you already know `fork`.

---

## Project: Static File HTTP Server

Build an HTTP/1.1 server that serves files from a directory.

### Spec

```bash
./httpd ./www 8080
```

- Listens on port 8080
- Serves files from `./www/`
- `GET /` → serve `./www/index.html`
- `GET /foo.html` → serve `./www/foo.html`
- File not found → `404 Not Found`
- Any other method → `405 Method Not Allowed`

Test with:
```bash
curl -v http://localhost:8080/
curl -v http://localhost:8080/index.html
curl -v http://localhost:8080/doesnotexist
```

### How to think about building this

**Step 1: Get a socket listening.**

Ignore HTTP entirely. Write a server that accepts a connection and sends back `"Hello!\n"` and closes. Test with `nc localhost 8080` or `curl`. Get comfortable with the socket lifecycle before adding complexity.

**Step 2: Read and print the HTTP request.**

After `accept`, read from the client fd into a buffer. Print what you got. You'll see the raw HTTP request text from curl/your browser. This demystifies HTTP — it's just text.

**Step 3: Parse the request line.**

Extract the method and path from the first line. `sscanf(line, "%s %s %s", method, path, version)` is the quickest way. Validate that it's a GET request.

**Step 4: Map the path to a file.**

Join your base directory with the requested path. Be careful: you must reject paths containing `..` to prevent directory traversal (a real security bug). Check if the path is `/` and map it to `index.html`.

**Step 5: Serve the file.**

Open the file. Get its size with `stat`. Send the status line, headers, and file contents. Close the client fd.

**Step 6: 404 handling.**

If `open` fails (file doesn't exist), send a `404 Not Found` response with a small HTML body.

**Step 7: Loop.**

Wrap the whole thing in a `while(1)` loop around `accept`. Test that multiple sequential requests work.

### Test files to create

Create a `www/` directory with:
- `www/index.html` — a simple HTML page
- `www/style.css` — a CSS file referenced from the HTML
- `www/hello.txt` — a plain text file

Open `http://localhost:8080/` in a browser. Watch your server's terminal output.

### Security note: path traversal

A request for `GET /../../../etc/passwd HTTP/1.1` would, if you naively join paths, try to serve a system file. Always check that the resolved path starts with your base directory. This is a real class of vulnerability — good practice to handle it even in a toy server.

### Testing your HTTP server

The server is an integration-test problem — its correctness is visible at the HTTP level, not at the function level. Use `curl` as your test client and a shell script as your runner.

**Unit test what you can — the path resolver:**
The function that maps a URL path to a filesystem path (including the `..` rejection logic) is pure string manipulation. Extract it into a testable function and use `CHECK` from `test.h`:
- `"/"` → `"./www/index.html"`
- `"/foo.html"` → `"./www/foo.html"`
- `"/../etc/passwd"` → rejected (returns NULL or an error code)
- `"/a/../../etc/passwd"` → rejected

**Integration test with `curl` and a script:**
Start your server in the background, run checks, kill it. Create `test.sh`:
```bash
./httpd ./www 8080 &
SERVER_PID=$!
sleep 0.2   # give it a moment to bind

check() {
    local desc="$1" expected="$2"
    local actual
    actual=$(eval "$3")
    if [ "$actual" = "$expected" ]; then
        echo "PASS  $desc"
    else
        echo "FAIL  $desc (got: $actual)"
    fi
}

check "200 for index"    "200" "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/"
check "404 for missing"  "404" "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/nope"
check "405 for POST"     "405" "curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:8080/"

kill $SERVER_PID
```

This pattern — start server, run checks, kill server — is how real HTTP server test suites work (just more elaborate).

**What to verify manually in a browser:**
- Open `http://localhost:8080/` — page renders correctly, CSS loads
- Open DevTools Network tab — check that `Content-Type` and `Content-Length` are correct for each file type

### Stretch goals
- Fork per connection (handle multiple clients simultaneously)
- Directory listing when the path is a directory and no `index.html` exists
- `Last-Modified` and `If-Modified-Since` headers (304 Not Modified)
- Serve a CGI script: if the path starts with `/cgi/`, fork and exec the script
