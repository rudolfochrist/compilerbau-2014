
%option noyywrap
%%

^[ \t]+  printf("");
[ \t]+$  printf("");
[ \t]+   printf(" ");

%%
int main(void)
{
  yylex();
  return 0;
}
