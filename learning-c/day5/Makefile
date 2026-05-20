CC = gcc
CFLAGS = -Wall -Wextra -g

httpd: httpd.c
	$(CC) $(CFLAGS) httpd.c -o httpd

test: httpd
	bash test.sh

clean:
	rm -f httpd
