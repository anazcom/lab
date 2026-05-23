#include "list.h"
#include <stdio.h>
#include <stdlib.h>

static int extend(List *l) {

  int new_capacity = 2 * l->capacity;
  int *new_data = (int *)realloc(l->data, new_capacity * sizeof(int));

  if (new_data == NULL) {
    perror("ERROR: list - unable to allocate memory");
    return 1;
  }

  l->capacity = new_capacity;
  l->data = new_data;
  return 0;
}

List list_new() {
  return (List){.capacity = LIST_DEFAULT_SIZE,
                .size = 0,
                .data = (int *)malloc(LIST_DEFAULT_SIZE * sizeof(int))};
}

int list_push(List *l, int value) {

  if (l == NULL) {
    perror("ERROR: list_push - Null Pointer Exception");
    return 1;
  }

  if (l->size >= l->capacity) {
    int result = extend(l);
    if (result > 0) {
      return 1;
    }
  }

  l->data[l->size++] = value;
  return 0;
}

int list_get(List *l, int idx, int *out) {
  if (l == NULL) {
    perror("ERROR: list_get - Null Pointer Exception");
    return 1;
  }

  if (l->size == 0 || idx >= l->size) {
    return 1;
  }
  *out = l->data[idx];
  return 0;
}

int list_set(List *l, int idx, int value) {
  if (l == NULL) {
    perror("ERROR: list_set - Null Pointer Exception");
    return 1;
  }

  if (l->size == 0 || idx >= l->size) {
    return 1;
  }

  l->data[idx] = value;
  return 0;
}

int list_len(List *l, int *out) {

  if (l == NULL) {
    perror("ERROR: list_len - Null Pointer Exception");
    return 1;
  }

  *out = l->size;
  return 0;
}

void list_free(List *l) {
  free(l->data);

  l->capacity = 0;
  l->size = 0;
  l->data = NULL;
}
