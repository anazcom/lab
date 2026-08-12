CC = clang
CFLAGS = -Wall -Wextra -g
ASAN_FLAGS = -fsanitize=address

mysh: mysh.c
	$(CC) $(CFLAGS) mysh.c -o mysh

asan: mysh.c
	$(CC) $(CFLAGS) $(ASAN_FLAGS) mysh.c -o mysh_asan

run_asan: asan
	MallocStackLogging=1 ./mysh_asan

parse_test: parse.c parse_test.c
	$(CC) $(CFLAGS) parse.c parse_test.c -o parse_test

test: parse_test
	./parse_test
	bash test.sh

clean:
	rm -f mysh mysh_asan parse_test
