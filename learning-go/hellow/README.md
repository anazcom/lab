# Notes
- Go manages dependencies through a file called `go.mod`. It is required on any project, and it should be the first thing to be setup. You can run `go mod init (name-of-your-module)`. You can see [here](https://go.dev/doc/tutorial/getting-started) for more details.

```bash
go mod init lab/goshell
```

- all files in the same directory should be grouped by the same package name (on root project we use `main`)

```go
package main

import "fmt"

func main(){
    fmt.Printl("hello, world!");
}
```

- to run the code we use `go run .`

- `go mod tidy` can be used to download dependencies when that are already being used.
