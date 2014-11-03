
%option noyywrap
%{
  int number_of_digrams = 0;
%}

%%

[a-z]         REJECT;
[a-z][a-z]    number_of_digrams++; REJECT;

%%

int main(int argc, char *argv[])
{
  yylex();
  printf("Number of digrams: %d\n", number_of_digrams);
  return 0;
}
