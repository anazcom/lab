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

int printFile(FILE *f, int feol, int fnumber, int *linesPrinted) {

  int bytesRead;
  char buffer[1024];
  int lines = *linesPrinted;
  int atStart = 1;

  while ((bytesRead = fread(buffer, 1, sizeof(buffer) - 1, f)) > 0) {

    buffer[bytesRead] = '\0';
    for (int i = 0; i < bytesRead; i++) {

      if (fnumber > 0 && atStart == 1) {
        printf("%d\t", ++lines);
        atStart = 0;
      }

      if (buffer[i] == '\n') {
        atStart = 1;
        if (feol > 0) {
          printf("$");
        }
      }
      printf("%c", buffer[i]);
    }
  }

  *linesPrinted = lines;
  return 0;
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
  int lines = 0;

  if (cfg.pathsCount == 0) {
    printFile(stdin, cfg.eof, cfg.number, &lines);
    return 0;
  }

  for (int i = 0; i < cfg.pathsCount; i++) {

    FILE *f = fopen(cfg.paths[i], "r");

    if (f == NULL) {
      perror("ERROR: mycat - file not found");
      return 1;
    }

    printFile(f, cfg.eof, cfg.number, &lines);
    fclose(f);
  }
  return 0;
}
