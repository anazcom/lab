## Read from StdIn


```go
import (
  "bufio"
)

command, err := bufio.NewReader(os.Stdin).ReadString('\n')
```
