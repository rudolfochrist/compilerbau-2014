
%%

%{
  private Parser parser;

  public Lexer(java.io.Reader r, Parser parser) {
      this(r);
      this.parser = parser;
  }

  public Yytoken t(Parser.Types type) {
      return new Yytoken(type);
  }

  public Yytoken t(Parser.Types type, int value) {
      return new Yytoken(type, value);
  }

  public Yytoken t(Parser.Types type, String value) {
      return new Yytoken(type, value);
  }

  public void print(String sym) {
    System.out.println(sym);
  }
%}

%class Lexer
%line

%%

"true"            { return t(Parser.Types.CONST_TRUE); }
"false"           { return t(Parser.Types.CONST_FALSE); }
[0-9]+            { return t(Parser.Types.LIT_NUMBER); }
"="               { return t(Parser.Types.OP_EQ); }
"!="              { return t(Parser.Types.OP_NEQ); }
"<"               { return t(Parser.Types.OP_LT); }
"<="              { return t(Parser.Types.OP_LE); }
">"               { return t(Parser.Types.OP_GT); }
">="              { return t(Parser.Types.OP_GE); }
"+"               { return t(Parser.Types.OP_PLUS); }
"-"               { return t(Parser.Types.OP_MINUS); }
"or"              { return t(Parser.Types.OP_OR); }
"*"               { return t(Parser.Types.OP_MUL); }
"/"               { return t(Parser.Types.OP_DIV); }
"and"             { return t(Parser.Types.OP_AND); }
"("               { return t(Parser.Types.OPEN_ROUND); }
")"               { return t(Parser.Types.CLOSE_ROUND); }
"not"             { return t(Parser.Types.OP_NOT); }
"{"               { return t(Parser.Types.OPEN_BRACE); }
"}"               { return t(Parser.Types.CLOSE_BRACE); }
"return"          { return t(Parser.Types.KEYWORD_RETURN); }
","               { return t(Parser.Types.COMMA); }
"while"           { return t(Parser.Types.KEYWORD_WHILE); }
"else"            { return t(Parser.Types.KEYWORD_ELSE); }
"if"              { return t(Parser.Types.KEYWORD_IF); }
":="              { return t(Parser.Types.OP_ASSIGNMENT); }
"int"             { return t(Parser.Types.TYPE_INT); }
"bool"            { return t(Parser.Types.TYPE_BOOL); }
";"               { return t(Parser.Types.SEMICOLON); }
"then"            { return t(Parser.Types.KEYWORD_THEN); }
"fi"              { return t(Parser.Types.KEYWORD_ENDIF); }
"\n"              { System.out.println(); }

[a-z][a-zA-Z0-9_]* { return t(Parser.Types.IDENTIFIER); }

"/\*.*\*/"        { print(yytext()); }

[\t ]+ { /* ignore whitespace */ }

. { print("error: unexpected token " + yytext()); }
