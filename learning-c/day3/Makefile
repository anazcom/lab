CC = gcc
CFLAGS = -Wall -Wextra -g -fsanitize=address

mysh: mysh.c
	$(CC) $(CFLAGS) mysh.c -o mysh

parse_test: parse.c parse_test.c
	$(CC) $(CFLAGS) parse.c parse_test.c -o parse_test

test: parse_test
	./parse_test
	bash test.sh

clean:
	rm -f mysh parse_test
