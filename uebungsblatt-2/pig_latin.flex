
%option noyywrap

%{
char character = '\0';
char vowel = '\0';
%}

VOWEL [aeiouAEIOU]
CHARACTER [bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ]

%%

{CHARACTER} {
if (character == '\0' && vowel == '\0') {
  character = *(yytext);
} else {
  printf("%s", yytext);
}
}

[ ]  {
  if (character != '\0') {
    printf("%cay ", character);
}

if (vowel != '\0') {
  printf("ay ");
}

character = '\0';
vowel = '\0';
}

\n  {
  if (character != '\0') {
    printf("%cay\n", character);
}

if (vowel != '\0') {
  printf("ay\n");
}

character = '\0';
vowel = '\0';
}

{VOWEL} {
if (vowel == '\0' && character == '\0') {
  vowel = *(yytext);
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
