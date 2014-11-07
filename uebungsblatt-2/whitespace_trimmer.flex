
%option noyywrap
%option debug
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
