
%option noyywrap
%option debug
%{
char character = 0;
char vowel = 0;
%}

VOWEL [aeiouAEIOU]
CHARACTER [bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ]

%%

{CHARACTER} {
if (!character && !vowel) {
  character = *yytext;
} else {
  printf("%s", yytext);
}
}

[ ]  {
  if (character) {
    printf("%c", character);
}

printf("ay ");
character = 0;
vowel = 0;
}

\n  {
if (character) {
    printf("%cay\n", character);
}

if (vowel) {
    printf("ay\n");
}

character = 0;
vowel = 0;
}

{VOWEL} {
if (!vowel && !character) {
  vowel = *yytext;
  printf("%s", yytext);
 } else {
  printf("%s", yytext);
 }
}

%%

int main(void)
{
  yylex();
  return 0;
}
