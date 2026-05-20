CC = gcc
CFLAGS = -Wall -Wextra -g -fsanitize=address

kv: kv.c
	$(CC) $(CFLAGS) kv.c -o kv

kv_test: kv.c kv_test.c
	$(CC) $(CFLAGS) kv.c kv_test.c -o kv_test

test: kv_test
	./kv_test

clean:
	rm -f kv kv_test
