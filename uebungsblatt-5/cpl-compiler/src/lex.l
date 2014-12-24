
%%

%{
  public void print(String sym) {
    System.out.println(sym);
  }
%}

%class Lexer
%line
%standalone

%%

"true"            { return Parser.CONST_TRUE; }
"false"           { return Parser.CONST_FALSE; }
[0-9]+            { return yytext(); }
"="               { return Parser.OP_EQ; }
"!="              { return Parser.OP_NEQ; }
"<"               { return Parser.OP_LT; }
"<="              { return Parser.OP_LE; }
">"               { return Parser.OP_GT; }
">="              { return Parser.OP_GE; }
"+"               { return Parser.OP_PLUS; }
"-"               { return Parser.OP_MINUS; }
"or"              { return Parser.OP_OR; }
"*"               { return Parser.OP_MUL; }
"/"               { return Parser.OP_DIV; }
"and"             { return Parser.OP_AND; }
"("               { return Parser.OPEN_ROUND; }
")"               { return Parser.CLOSE_ROUND; }
"not"             { return Parser.OP_NOT; }
"{"               { return Parser.OPEN_BRACE; }
"}"               { return Parser.CLOSE_BRACE; }
"return"          { return Parser.KEYWORD_RETURN; }
","               { return Parser.COMMA; }
"while"           { return Parser.KEYWORD_WHILE; }
"else"            { return Parser.KEYWORD_ELSE; }
"if"              { return Parser.KEYWORD_IF; }
":="              { return Parser.OP_ASSIGNMENT; }
"int"             { return Parser.TYPE_INT; }
"bool"            { return Parser.TYPE_BOOL; }
";"               { return Parser.SEMICOLON; }
"\n"              { /* ignore it for now */ }

[a-z][a-zA-Z0-9_]* {
  if (yytext().length() < 8) {
    print("identifier " + yytext());
  } else {
    print("indentifier " + yytext().substring(0, 8));
  }
}
"/\*.*\*/"        { print(yytext()); }
[\t ]+ { /* ignore whitespace */ }
. { print("error: unexpected token " + yytext()); }
