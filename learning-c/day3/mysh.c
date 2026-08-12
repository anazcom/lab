
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#define MYSH_DEFAULT_PROMPT "mysh>"
#define MYSH_MAX_BUFFER_SIZE 1024
#define MYSH_MAX_TOKEN_SIZE 60

int read_input(char *buffer, size_t size) {

  if (fgets(buffer, size, stdin) == NULL) {
    if (feof(stdin)) {
      printf("\n");
      return 0;
    }
    perror("fgets");
    return -1;
  }

  size_t buffer_len = strlen(buffer);
  if (buffer[buffer_len - 1] != '\n') {
    int c;
    while ((c = getchar()) != '\n' && c != EOF)
      ; // consume the remaining characters
    fprintf(stderr, "ERROR: input line too long (Limit: %zu) \n", size);
    return 1;
  }

  buffer[buffer_len - 1] = '\0';
  return 0;
}

typedef enum { NORMAL, SINGLE_QUOTE, DOUBLE_QUOTE } ParseState;

char **parse(char *input) {

  char **tokens = calloc(MYSH_MAX_TOKEN_SIZE, sizeof(char *));
  size_t tokens_len = 0;
  char buffer[MYSH_MAX_BUFFER_SIZE];
  size_t buffer_len = 0;
  bool end = false;
  ParseState state = NORMAL;

  for (const char *p = input;; p++) {
    char c = *p;

    if (end) {
      return tokens;
    }

    if (c == '\0') {
      if (state != NORMAL) {
        fprintf(stderr, "ERROR: Parse ending in bad state\n");
        free(tokens);
        return NULL;
      }
      end = true;
    }

    switch (state) {
    case (SINGLE_QUOTE): {
      if (c == '\'') {
        state = NORMAL;
        fprintf(stdout, "\tSingle Quote: going back to NORMAL\n");
      } else {
        fprintf(stdout, "\tSingle Quote: accumulate char %c\n", c);
        buffer[buffer_len++] = c;
      }
      break;
    }
    case (DOUBLE_QUOTE): {
      if (c == '"') {
        state = NORMAL;
        fprintf(stdout, "\tDouble Quote: going back to NORMAL\n");
      } else if (c == '\\' && (p[1] == '"' || p[1] == '\\')) {
        buffer[buffer_len++] = *++p;
        fprintf(stdout, "\tDouble Quote: quote or backslash case (%c)\n", c);
      } else {
        buffer[buffer_len++] = c;
        fprintf(stdout, "\tDouble Quote: accumulate char %c\n", c);
      }
      break;
    }
    case (NORMAL): {
      if (c == ' ' || c == '\t' || c == '\0') {
        if (buffer_len > 0) {
          tokens[tokens_len++] = strndup(buffer, buffer_len);
          buffer_len = 0;
          fprintf(stdout, "Normal: saving token to tokens(%s)\n",
                  tokens[tokens_len - 1]);
        }
      } else if (c == '\'') {
        fprintf(stdout, "Normal: starting single quote\n");
        state = SINGLE_QUOTE;
      } else if (c == '"') {
        fprintf(stdout, "Normal: starting double quote\n");
        state = DOUBLE_QUOTE;
      } else {
        fprintf(stdout, "Normal: accumulate char (%c)\n", c);
        buffer[buffer_len++] = c;
      }
      break;
    }
    }

    if (buffer_len >= MYSH_MAX_BUFFER_SIZE - 1 ||
        tokens_len >= MYSH_MAX_TOKEN_SIZE - 1) {
      free(tokens);
      return NULL;
    }
  }

  tokens[tokens_len] = NULL; // to be able to iterate until the null pointer
  return tokens;
}

void echo(char **tokens) {
  for (size_t i = 0; tokens[i]; i++) {
    fprintf(stdout, "%s ", tokens[i]);
  }
  fprintf(stdout, "\n");
}

// output needs to be freed out;
char *find_path_for(char *program) {

  if (strchr(program, '/') != NULL) {
    return access(program, X_OK) == 0 ? strdup(program) : NULL;
  }

  char *path_env = getenv("PATH");
  if (path_env == NULL) {
    return NULL;
  }

  char *path_copy = strdup(path_env);
  if (path_copy == NULL) {
    return NULL;
  }

  char candidate[MYSH_MAX_BUFFER_SIZE];
  char *dir = strtok(path_copy, ":");
  while (dir != NULL) {
    snprintf(candidate, sizeof(candidate), "%s/%s", dir, program);
    if (access(candidate, X_OK) == 0) {
      free(path_copy);
      return strdup(candidate);
    }
    dir = strtok(NULL, ":");
  }

  free(path_copy);
  return NULL;
}

void process(char **argv) {

  if (argv[0] == NULL) {
    return;
  }

  char *file = find_path_for(argv[0]);
  if (file == NULL) {
    fprintf(stderr, "mysh: command not found: %s\n", argv[0]);
    return;
  }
  execvp(file, argv + 1);
  free(file);
}

int eval(char **tokens) {

  size_t si = 0;

  for (size_t i = 0; tokens[i]; i++) {
    if (strcmp(tokens[i], "|") == 0) {
      tokens[i] = NULL;
      char **command = (tokens + si);
      si = i + 1;

      process(command);
    }
  }

  process(tokens + si);

  return 0;
}

int main() {
  size_t buffer_len = MYSH_MAX_BUFFER_SIZE;
  char buffer[buffer_len];

  while (1) {
    printf("%s ", MYSH_DEFAULT_PROMPT);
    fflush(stdout);

    int rc = read_input(buffer, buffer_len);
    if (rc < 0) {
      break;
    }
    if (rc > 0) {
      continue; // line too long
    }
    char **tokens = parse(buffer);
    eval(tokens); // not sure if this should use eval
                  // in here or split the tokens in command
                  // before.
    free(tokens);
  }
  return 0;
}
