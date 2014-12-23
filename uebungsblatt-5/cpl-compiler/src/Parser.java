import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Parser {

    private Lexer scanner;
    private HashMap<String, Object> symbols;

    public Parser(Lexer scanner) {
        this.scanner = scanner;
        symbols = new HashMap<String, Object>();
    }

    private boolean match(String token) {
        if (token.equals(scanner.yytext())) {
            try {
                scanner.yylex();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean matchId() {
        if (lookup(scanner.yytext()) != null) {
            match(scanner.yytext());
            return true;
        }
        return false;
    }

    private Object lookup(String symbol) {
        return symbols.get(symbol);
    }

    private boolean peek(String token) {
        return token.equals(scanner.yytext());
    }

    private boolean peekId() {
        return lookup(scanner.yytext()) != null;
    }


    public void parse() {
        try {
            scanner.yylex();
            program();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void program() {
        if (peek("int") || peek("bool")) {
            decl_part();
        }
    }

    private void decl_part() {
        if (peek("int") || peek("bool")) {
            type_id();
            decl_part_rest();
        }
    }

    private void decl_part_rest() {
        if (peek("(")) {
            decl_part_func();
        } else if (peek(",")) {
            var_decl();
            decl_part_func();
        }
    }

    private void decl_part_func() {
        if (peek("(")) {
            func_decl();
            decl_part_func_rest();
        }
    }

    private void decl_part_func_rest() {
        if (peek("int") || peek("bool")) {
            type_id();
            decl_part_func();
        }
    }

    private void type_id() {
        if (peek("int") || peek("bool")) {
            type();
            matchId();
        }
    }

    private void var_decl() {
        if (peek(",")) {
            id_list();
            match(";");
            type_id();
            var_decl_rest();
        }
    }
    private void var_decl_rest() {
        if (peek(",")) {
            var_decl();
        }
    }

    private void type() {
        if (peek("int")) {
            match("int");
        } else if (peek("bool")) {
            match("bool");
        }
    }

    private void id_list() {
        if (peek(",")) {
            match(",");
            matchId();
            id_list();
        }
    }

    private void func_decl() {
        if (peek("(")) {
            match("(");
            params();
            match(")");
            body();
        }
    }

    private void params() {
        if (peek("int") || peek("bool")) {
            param_list();
        }
    }

    private void param_list() {
        if (peek("int") || peek("bool")) {
            type();
            matchId();
            param_list_rest();
        }
    }

    private void param_list_rest() {
        if (peek(",")) {
            match(",");
            param_list();
        }
    }

    private void body() {
        if (peek("{")) {
            match("{");
            var_decl();
            stmt_seq();
            match("}");
        }
    }

    private void stmt_seq() {
        if (peekId() || peek("return") || peek("{") || peek("if") || peek("while")) {
            stmt();
            stmt_seq();
        }
    }

    private void stmt() {
        if (peekId() || peek("return")) {
            simple_stmt();
            match(";");
        } else if (peek("{") || peek("if") || peek("while")) {
            struct_stmt();
        }
    }

    private void simple_stmt() {
        if (peekId()) {
            assignment_or_func_call();
        } else if (peek("return")) {
            return_stmt();
        }
    }

    private void assignment_or_func_call() {
        if (peekId()) {
            matchId();
            assignment_or_func_call_rest();
        }
    }

    private void assignment_or_func_call_rest() {
        if (peek(":=")) {
            assignment();
        }
    }

    private void struct_stmt() {
        if (peek("{")) {
            comp_stmt();
        } else if (peek("if")) {
            cond();
        } else if (peek("while")) {
            loop();
        }
    }

    private void assignment() {
        if (peek(":=")) {
            match(":=");
            expr();
        }
    }

    private void cond() {
        if (peek("if")) {
            match("if");
            match("(");
            expr();
            match(")");
            match("then");
            stmt();
            cond_rest();
        }
    }

    private void cond_rest() {
        if (peek("fi")) {
            match("fi");
        } else if (peek("else")) {
            match("else");
            stmt();
            match("fi");
        }
    }

    private void loop() {
        if (peek("while")) {
            match("while");
            match("(");
            expr();
            match(")");
            stmt();
        }
    }

    private void func_call() {
        if (peek("(")) {
            match("(");
            args();
            match(")");
        }
    }

    private void args() {
        if (peek("true") || peek("false") || peekId() || peek("(") || peek("+") || peek("-") || peek("not") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            arg_list();
        }
    }

    private void arg_list() {
        if (peek("true") || peek("false") || peekId() || peek("(") || peek("+") || peek("-") || peek("not") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            expr();
            arg_list_rest();
        }
    }

    private void arg_list_rest() {
        if (peek(",")) {
            match(",");
            arg_list();
        }
    }

    private void return_stmt() {
        if (peek("return")) {
            match("return");
            expr();
        }
    }

    private void comp_stmt() {
        if (peek("{")) {
            match("{");
            stmt_seq();
            match("}");
        }
    }

    private void expr() {
        if (peek("true") || peek("false") || peekId() || peek("(") || peek("+") || peek("-") || peek("not") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            simple_expr();
            expr_rest();
        }
    }

    private void expr_rest() {
        if (peek("=") || peek("!=") || peek("<") || peek("<=") || peek(">") || peek(">=")) {
            rel_op();
            simple_expr();
        }
    }

    private void simple_expr() {
        if (peek("true") || peek("false") || peekId() || peek("(") || peek("+") || peek("-") || peek("not") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            term();
            simple_expr_rest();
        }
    }

    private void simple_expr_rest() {
        if (peek("+") || peek("-") || peek("or")) {
            add_op();
            term();
            simple_expr_rest();
        }
    }

    private void term() {
        if (peek("true") || peek("false") || peekId() || peek("(") || peek("+") || peek("-") || peek("not") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            factor();
            term_rest();
        }
    }

    private void term_rest() {
        if (peek("*") || peek("/") || peek("and")) {
            mul_op();
            factor();
            term_rest();
        }
    }

    private void factor() {
        if (peek("true") || peek("false") ||
                peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            const_val();
        } else if (peek("(")) {
            match("(");
            expr();
            match(")");
        } else if (peekId()) {
            id_or_func_call();
        } else if (peek("+") || peek("-")) {
            sign();
            factor();
        } else if (peek("not")) {
            match("not");
            factor();
        }
    }

    private void id_or_func_call() {
        if (peekId()) {
            matchId();
            id_or_func_call_rest();
        }
    }

    private void id_or_func_call_rest() {
        if (peek("(")) {
            func_call();
        }
    }

    private void sign() {
        if (peek("+")) {
            match("+");
        } else if (peek("-")) {
            match("-");
        }
    }

    private void mul_op() {
        if (peek("*")) {
            match("*");
        } else if (peek("/")) {
            match("/");
        } else if (peek("and")) {
            match("and");
        }
    }

    private void add_op() {
        if (peek("+")) {
            match("+");
        } else if (peek("-")) {
            match("-");
        } else if (peek("or")) {
            match("or");
        }
    }

    private void rel_op() {
        if (peek("=")) match("=");
        else if (peek("!=")) match("!=");
        else if (peek("<")) match("<");
        else if (peek("<=")) match("<=");
        else if (peek(">")) match(">");
        else if (peek(">=")) match(">=");
    }

    private void const_val() {
        if (peek("true") || peek("false")) {
            bool_const();
        } else if (peek("0") || peek("1") ||peek("2") ||peek("3") ||peek("4") ||peek("5") ||peek("6") ||peek("7") ||peek("8") ||peek("9")) {
            number();
        }
    }

    private void number() {
        if (peek("0")) match("0");
        else if (peek("1")) match("1");
        else if (peek("2")) match("2");
        else if (peek("3")) match("3");
        else if (peek("4")) match("4");
        else if (peek("5")) match("5");
        else if (peek("6")) match("6");
        else if (peek("7")) match("7");
        else if (peek("8")) match("8");
        else if (peek("9")) match("9");
    }

    private void bool_const() {
        if (peek("true")) match("true");
        else if (peek("false")) match("false");
    }


    public static void main(String[] argv) throws FileNotFoundException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(argv[0]));
        new Parser(new Lexer(reader)).parse();
    }
}
