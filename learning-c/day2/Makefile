CC = gcc
CFLAGS = -Wall -Wextra -g -fsanitize=address

vec_test: vec.c vec_test.c
	$(CC) $(CFLAGS) vec.c vec_test.c -o vec_test

test: vec_test
	./vec_test

clean:
	rm -f vec_test
