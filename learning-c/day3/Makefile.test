CC = clang
CFLAGS = -Wall -Wextra -g -fsanitize=address

TEST_SRCS := $(wildcard test_*.c)
TEST_BINS := $(TEST_SRCS:.c=)

.PHONY: test clean
test: $(TEST_BINS)
	@echo "\n--- Test Execution ----"
	@for t in $(TEST_BINS); do ./$$t; done

test_%: test_%.o list.o
	$(CC) $(CFLAGS) -o $@ $^

%.o: %.c
	$(CC) $(CFLAGS) -c $<

clean: 
	rm -f *.o *.d $(TEST_BINS)
