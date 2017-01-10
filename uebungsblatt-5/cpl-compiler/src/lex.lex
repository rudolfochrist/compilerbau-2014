import java.io.PrintWriter;

%%

%{
  public int lineno = 1;
  private PrintWriter writer;
  public final Messages messages = new Messages();

  public Yytoken t(Parser.Types type) {
      return new Yytoken(type);
  }

  public Yytoken t(Parser.Types type, String value) {
      return new Yytoken(type, value);
  }

  public void print(String sym) {
    if (writer == null) writer = new PrintWriter(System.out);
    writer.print(sym);
    writer.flush();
  }

  public Lexer(java.io.Reader r, PrintWriter writer) {
    this(r);
    this.writer = writer;
  }

%}

%init{
    print("" + lineno);
    print(": ");
%init}

%class Lexer
%line
%debug
%type Yytoken

%%

//"/\*.*\*/"      {print("comment: "); }

"/*" [^*] ~"*/"   {print(yytext());}
"/*" "*"+ "/"     {print(yytext());}


"true"            { print(yytext()); return t(Parser.Types.CONST_TRUE); }
"false"           { print(yytext()); return t(Parser.Types.CONST_FALSE); }
[0-9]+            { print(yytext()); return t(Parser.Types.LIT_NUMBER, yytext()); }
"="               { print(yytext()); return t(Parser.Types.OP_EQ); }
"!="              { print(yytext()); return t(Parser.Types.OP_NEQ); }
"<"               { print(yytext()); return t(Parser.Types.OP_LT); }
"<="              { print(yytext()); return t(Parser.Types.OP_LE); }
">"               { print(yytext()); return t(Parser.Types.OP_GT); }
">="              { print(yytext()); return t(Parser.Types.OP_GE); }
"+"               { print(yytext()); return t(Parser.Types.OP_PLUS); }
"-"               { print(yytext()); return t(Parser.Types.OP_MINUS); }
"or"              { print(yytext()); return t(Parser.Types.OP_OR); }
"*"               { print(yytext()); return t(Parser.Types.OP_MUL); }
"/"               { print(yytext()); return t(Parser.Types.OP_DIV); }
"and"             { print(yytext()); return t(Parser.Types.OP_AND); }
"("               { print(yytext()); return t(Parser.Types.OPEN_ROUND); }
")"               { print(yytext()); return t(Parser.Types.CLOSE_ROUND); }
"not"             { print(yytext()); return t(Parser.Types.OP_NOT); }
"{"               { print(yytext()); return t(Parser.Types.OPEN_BRACE); }
"}"               { print(yytext()); return t(Parser.Types.CLOSE_BRACE); }
"return"          { print(yytext()); return t(Parser.Types.KEYWORD_RETURN); }
","               { print(yytext()); return t(Parser.Types.COMMA); }
"while"           { print(yytext()); return t(Parser.Types.KEYWORD_WHILE); }
"else"            { print(yytext()); return t(Parser.Types.KEYWORD_ELSE); }
"if"              { print(yytext()); return t(Parser.Types.KEYWORD_IF); }
":="              { print(yytext()); return t(Parser.Types.OP_ASSIGNMENT); }
"int"             { print(yytext()); return t(Parser.Types.TYPE_INT); }
"bool"            { print(yytext()); return t(Parser.Types.TYPE_BOOL); }
";"               { print(yytext()); return t(Parser.Types.SEMICOLON); }
"then"            { print(yytext()); return t(Parser.Types.KEYWORD_THEN); }
"fi"              { print(yytext()); return t(Parser.Types.KEYWORD_ENDIF); }
"\n"|"\r\n"       { print(yytext()); messages.print(lineno, writer); print("" + ++lineno); print(": "); }

[a-z][a-zA-Z0-9_]* {
                   print(yytext());
                   if (yytext().length() > 8) {
                      messages.warning(lineno, "Identifier '"+yytext()+"' is too long. It will be truncated to the first 8 characters '"+yytext().substring(0,8)+"'.");
                      return t(Parser.Types.IDENTIFIER, yytext().substring(0, 8));
                   }
                   return t(Parser.Types.IDENTIFIER, yytext()); }

[\t ]+ { print(yytext()); }

. { print(yytext()); messages.warning(lineno, "Unexpected character found: " + yytext()); }
