#include "list.h"
#include "testy.h"

void test_listnew() {

  List list = list_new();
  CHECK(list.capacity == LIST_DEFAULT_SIZE);
  CHECK(list.size == 0);
}

void test_listpush() {
  List list = list_new();
  list_push(&list, 3);

  CHECK(list.capacity == LIST_DEFAULT_SIZE);
  CHECK(list.size == 1);
}

void test_listpush_reallocation() {
  List list = list_new();
  for (int i = 0; i < 10; i++) {
    list_push(&list, i);
  }

  list_push(&list, 11);
  // Expected to twice the capacity in each reallocation
  CHECK(list.capacity == 20);
  CHECK(list.size == 11);
}

void test_listget_emptyListError() {
  List list = list_new();
  int val = 0;
  int result = list_get(&list, 0, &val);

  CHECK(result == 1);
}

void test_listget() {
  List list = list_new();
  int val = 0;

  list_push(&list, 3);
  int result = list_get(&list, 0, &val);

  CHECK(result == 0);
  CHECK(val == 3);
}

void test_listset_emptyListError() {
  List list = list_new();

  int result = list_set(&list, 0, 1);

  CHECK(result == 1);
}

void test_listset() {
  List list = list_new();

  list_push(&list, 0);
  int result = list_set(&list, 0, 1);

  CHECK(result == 0);
  CHECK(list.data[0] == 1);
}

void test_listlen() {
  List list = list_new();

  CHECK(list.size == 0);
}

void test_listfree() {
  List list = list_new();
  list_free(&list);

  CHECK(list.size == 0);
  CHECK(list.capacity == 0);
  CHECK(list.data == NULL);
}

int main() {

  test_listnew();
  test_listpush();
  test_listpush_reallocation();
  test_listget_emptyListError();
  test_listget();
  test_listset_emptyListError();
  test_listset();
  test_listlen();
  test_listfree();

  REPORT();
  return 0;
}
