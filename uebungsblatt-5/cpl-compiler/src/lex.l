
%%

%{
  public void print(String sym) {
    System.out.println(sym);
  }
%}

%class Lexer
%line
%debug
%standalone

%%

"true"            { print("true_keyword"); }
"false"           { print("false_keyword"); }
[0-9]+            { print("number " + yytext()); }
"="               { print("eq_op"); }
"!="              { print("neq_op"); }
"<"               { print("lt_op"); }
"<="              { print("lte_op"); }
">"               { print("gt_op"); }
">="              { print("gte_op"); }
"+"               { print("plus_op"); }
"-"               { print("minus_op"); }
"or"              { print("or_op"); }
"*"               { print("mul_op"); }
"/"               { print("div_op"); }
"and"             { print("and_op"); }
"("               { print("open_round"); }
")"               { print("close_round"); }
"not"             { print("not_op"); }
"{"               { print("open_brace"); }
"}"               { print("close_brace"); }
"return"          { print("retrun_op"); }
","               { print("comma"); }
"while"           { print("while_keyword"); }
"else"            { print("else_keyword"); }
"if"              { print("if_keyword"); }
":="              { print("assignment"); }
"int"             { print("int_type"); }
"bool"            { print("bool_type"); }

[a-z][a-zA-Z0-9_] {
  if (yytext().length() < 8) {
    print("identifier " + yytext());
  } else {
    print("indentifier " + yytext().substring(0, 8));
  }
}
"/\*.*\*/"        { print(yytext()); }
