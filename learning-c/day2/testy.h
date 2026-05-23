#include <stdio.h>

static int _tests_run = 0;
static int _tests_failed = 0;

#define CHECK(cond)                                                            \
  do {                                                                         \
    _tests_run++;                                                              \
    if (!(cond)) {                                                             \
      printf("FAIL %s:%d\t%s\n", __FILE__, __LINE__, #cond);                   \
      _tests_failed++;                                                         \
    }                                                                          \
  } while (0)

#define REPORT()                                                               \
  do {                                                                         \
    printf("\n%d out of %d passed\n", _tests_run - _tests_failed, _tests_run); \
  } while (0)
