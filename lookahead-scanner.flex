
%option noyywrap
%{
int number_of_digrams = 0;
%}

%%

[^ \t\n]/[^ \t\n]  number_of_digrams++;

%%

int main(int argc, char *argv[])
{
  yylex();
  printf("Number of digrams: %d\n", number_of_digrams);
  return 0;
}
