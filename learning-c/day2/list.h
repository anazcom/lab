
typedef struct {
  int size;
  int capacity;
  int *data;
} List;

#define LIST_DEFAULT_SIZE 10

/*
 * Create a new List Object
 * */
List list_new();

/*
 * Add an element to the list
 *
 * @return: 0 success, 1 otherwise
 */
int list_push(List *l, int value);

/*
 * Return i-th element of the list
 *
 * @param l: List instance to get the data from
 * @param idx: Index of the element
 * @param out: Reference to the output
 *
 * @return 0: Success, value is on *out
 *         1: Failure
 */
int list_get(List *l, int idx, int *out);

int list_set(List *l, int idx, int value);

int list_len(List *l, int *out);

void list_free(List *l);
