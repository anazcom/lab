#include <assert.h>
#include <stdio.h>

int strCmp(char *s1, char *s2) {

  int i = 0;
  while (s1[i] == s2[i]) {
    if (s1[i] == '\0') {
      return 0;
    }
    i++;
  }
  return 1;
}

/*
 * Open and close a file specified by path or
 * stdin if path = "stdin"
 * */
void withFile(char *path, int feof, int fnumber, int start,
              int handler(FILE *, int, int, int)) {
  FILE *f;

  if (strCmp(path, "stdin") == 0) {
    printf("mycat: opened stdin\n");
    f = stdin;
  } else {
    f = fopen(path, "r");
    printf("mycat: opened file\n");
  }

  if (f == NULL) {
    perror("ERROR: mycat - file not found\n");
  }

  handler(f, feof, fnumber, start);
  fclose(f);
}

int printFile(FILE *f, int feof, int fnumber, int start) {

  int bytesRead;
  char buffer[1024];
  int lines = start;

  if (fnumber > 0) {
    printf("%d\t", ++lines);
  }

  while ((bytesRead = fread(buffer, 1, sizeof(buffer) - 1, f)) > 0) {

    buffer[bytesRead] = '\0';
    printf("reading %d bytes", bytesRead);
    for (int i = 0; i < bytesRead; i++) {
      if (buffer[i] == '\n' && feof > 0) {
        printf("$");
      }
      printf("%c", buffer[i]);
      if (buffer[i] == '\n' && fnumber > 0) {
        printf("%d\t", ++lines);
      }
    }
  }

  return lines - start;
}

void printUsage() { printf("mycat -[ne] [file...]\n"); }

typedef struct {
  int number;
  int eof;
  char *paths[20];
  int pathsCount;
} Config;

Config parse(int argc, char *argv[]) {

  Config cfg = {0};
  int fpaths = 0;

  for (int i = 1; i < argc; i++) {

    char *item = argv[i];
    if (fpaths == 0 && strCmp(item, "-n") == 0) {
      cfg.number++;
    } else if (fpaths == 0 && strCmp(item, "-e") == 0) {
      cfg.eof++;
    } else if (fpaths == 0 && strCmp(item, "--") == 0) {
      fpaths++;
    } else {
      cfg.paths[cfg.pathsCount++] = item;
    }
  }

  return cfg;
}

int main(int argc, char *argv[]) {

  Config cfg = parse(argc, argv);
  printf("EOF: %d Number: %d PCount: %d\n", cfg.eof, cfg.number,
         cfg.pathsCount);

  if (cfg.pathsCount == 0) {
    printf("mycat: no paths provided. Defaulting to stdin\n");
    printFile(stdin, cfg.eof, cfg.number, 0);
    return 0;
  }

  for (int i = 0; i < cfg.pathsCount; i++) {
    withFile(cfg.paths[i], cfg.eof, cfg.number, 0, printFile);
  }
  return 0;
}
