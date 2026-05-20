CC = clang
CFLAGS = -Wall -Wextra -g -fsanitize=address

mycat: mycat.c
	$(CC) $(CFLAGS) mycat.c -o mycat

clean:
	rm -f mycat
