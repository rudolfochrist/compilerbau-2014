import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Parser {

    public static final String TYPE_INT = "int";
    public static final String TYPE_BOOL = "bool";
    public static final String OPEN_ROUND = "(";
    public static final String COMMA = ",";
    public static final String SEMICOLON = ";";
    public static final String CLOSE_ROUND = ")";
    public static final String OPEN_BRACE = "{";
    public static final String CLOSE_BRACE = "}";
    public static final String KEYWORD_RETURN = "return";
    public static final String KEYWORD_IF = "if";
    public static final String KEYWORD_WHILE = "while";
    public static final String OP_ASSIGNMENT = ":=";
    public static final String KEYWORD_THEN = "then";
    public static final String KEYWORD_ENDIF = "fi";
    public static final String CONST_TRUE = "true";
    public static final String CONST_FALSE = "false";
    public static final String OP_PLUS = "+";
    public static final String OP_MINUS = "-";
    public static final String LIT_NUMBER = "lit_number";
    public static final String IDENTIFIER = "identifier";
    public static final String OP_EQ = "=";
    public static final String OP_NEQ = "!=";
    public static final String OP_LT = "<";
    public static final String OP_LE = "<=";
    public static final String OP_GT = ">";
    public static final String OP_GE = ">=";
    public static final String OP_MUL = "*";
    public static final String OP_DIV = "/";
    public static final String OP_AND = "and";
    public static final String OP_OR = "or";
    public static final String OP_NOT = "not";
    public static final String KEYWORD_ELSE = "else";
    private Lexer scanner;
    private HashMap<String, Object> symbols;
    private String currentToken;

    public Parser(Lexer scanner) {
        this.scanner = scanner;
        symbols = new HashMap<String, Object>();
    }

    private boolean match(String token) {
        if (token.equals(currentToken)) {
            try {
                currentToken = scanner.yylex();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean peek(String token) {
        return token.equals(currentToken);
    }

    public void parse() {
        try {
            currentToken = scanner.yylex();
            program();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void program() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            decl_part();
        }
    }

    private void decl_part() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            type_id();
            decl_part_rest();
        }
    }

    private void decl_part_rest() {
        if (peek(OPEN_ROUND)) {
            decl_part_func();
        } else if (peek(COMMA)) {
            var_decl();
            decl_part_func();
        }
    }

    private void decl_part_func() {
        if (peek(OPEN_ROUND)) {
            func_decl();
            decl_part_func_rest();
        }
    }

    private void decl_part_func_rest() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            type_id();
            decl_part_func();
        }
    }

    private void type_id() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            type();
            match(IDENTIFIER);
        }
    }

    private void var_decl() {
        if (peek(COMMA)) {
            id_list();
            match(SEMICOLON);
            type_id();
            var_decl_rest();
        }
    }
    private void var_decl_rest() {
        if (peek(COMMA)) {
            var_decl();
        }
    }

    private void type() {
        if (peek(TYPE_INT)) {
            match(TYPE_INT);
        } else if (peek(TYPE_BOOL)) {
            match(TYPE_BOOL);
        }
    }

    private void id_list() {
        if (peek(COMMA)) {
            match(COMMA);
            match(IDENTIFIER);
            id_list();
        }
    }

    private void func_decl() {
        if (peek(OPEN_ROUND)) {
            match(OPEN_ROUND);
            params();
            match(CLOSE_ROUND);
            body();
        }
    }

    private void params() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            param_list();
        }
    }

    private void param_list() {
        if (peek(TYPE_INT) || peek(TYPE_BOOL)) {
            type();
            match(IDENTIFIER);
            param_list_rest();
        }
    }

    private void param_list_rest() {
        if (peek(COMMA)) {
            match(COMMA);
            param_list();
        }
    }

    private void body() {
        if (peek(OPEN_BRACE)) {
            match(OPEN_BRACE);
            var_decl();
            stmt_seq();
            match(CLOSE_BRACE);
        }
    }

    private void stmt_seq() {
        if (peek(IDENTIFIER) || peek(KEYWORD_RETURN) || peek(OPEN_BRACE) || peek(KEYWORD_IF) || peek(KEYWORD_WHILE)) {
            stmt();
            stmt_seq();
        }
    }

    private void stmt() {
        if (peek(IDENTIFIER) || peek(KEYWORD_RETURN)) {
            simple_stmt();
            match(SEMICOLON);
        } else if (peek(OPEN_BRACE) || peek(KEYWORD_IF) || peek(KEYWORD_WHILE)) {
            struct_stmt();
        }
    }

    private void simple_stmt() {
        if (peek(IDENTIFIER)) {
            assignment_or_func_call();
        } else if (peek(KEYWORD_RETURN)) {
            return_stmt();
        }
    }

    private void assignment_or_func_call() {
        if (peek(IDENTIFIER)) {
            match(IDENTIFIER);
            assignment_or_func_call_rest();
        }
    }

    private void assignment_or_func_call_rest() {
        if (peek(OP_ASSIGNMENT)) {
            assignment();
        }
    }

    private void struct_stmt() {
        if (peek(OPEN_BRACE)) {
            comp_stmt();
        } else if (peek(KEYWORD_IF)) {
            cond();
        } else if (peek(KEYWORD_WHILE)) {
            loop();
        }
    }

    private void assignment() {
        if (peek(OP_ASSIGNMENT)) {
            match(OP_ASSIGNMENT);
            expr();
        }
    }

    private void cond() {
        if (peek(KEYWORD_IF)) {
            match(KEYWORD_IF);
            match(OPEN_ROUND);
            expr();
            match(CLOSE_ROUND);
            match(KEYWORD_THEN);
            stmt();
            cond_rest();
        }
    }

    private void cond_rest() {
        if (peek(KEYWORD_ENDIF)) {
            match(KEYWORD_ENDIF);
        } else if (peek(KEYWORD_ELSE)) {
            match(KEYWORD_ELSE);
            stmt();
            match(KEYWORD_ENDIF);
        }
    }

    private void loop() {
        if (peek(KEYWORD_WHILE)) {
            match(KEYWORD_WHILE);
            match(OPEN_ROUND);
            expr();
            match(CLOSE_ROUND);
            stmt();
        }
    }

    private void func_call() {
        if (peek(OPEN_ROUND)) {
            match(OPEN_ROUND);
            args();
            match(CLOSE_ROUND);
        }
    }

    private void args() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(IDENTIFIER) || peek(OPEN_ROUND) || peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_NOT) || peek(LIT_NUMBER)) {
            arg_list();
        }
    }

    private void arg_list() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(IDENTIFIER) || peek(OPEN_ROUND) || peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_NOT) || peek(LIT_NUMBER)) {
            expr();
            arg_list_rest();
        }
    }

    private void arg_list_rest() {
        if (peek(COMMA)) {
            match(COMMA);
            arg_list();
        }
    }

    private void return_stmt() {
        if (peek(KEYWORD_RETURN)) {
            match(KEYWORD_RETURN);
            expr();
        }
    }

    private void comp_stmt() {
        if (peek(OPEN_BRACE)) {
            match(OPEN_BRACE);
            stmt_seq();
            match(CLOSE_BRACE);
        }
    }

    private void expr() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(IDENTIFIER) || peek(OPEN_ROUND) || peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_NOT) || peek(LIT_NUMBER)) {
            simple_expr();
            expr_rest();
        }
    }

    private void expr_rest() {
        if (peek(OP_EQ) || peek(OP_NEQ) || peek(OP_LT) || peek(OP_LE) || peek(OP_GT) || peek(OP_GE)) {
            rel_op();
            simple_expr();
        }
    }

    private void simple_expr() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(IDENTIFIER) || peek(OPEN_ROUND) || peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_NOT) || peek(LIT_NUMBER)) {
            term();
            simple_expr_rest();
        }
    }

    private void simple_expr_rest() {
        if (peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_OR)) {
            add_op();
            term();
            simple_expr_rest();
        }
    }

    private void term() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(IDENTIFIER) || peek(OPEN_ROUND) || peek(OP_PLUS) || peek(OP_MINUS) || peek(OP_NOT) || peek(LIT_NUMBER)) {
            factor();
            term_rest();
        }
    }

    private void term_rest() {
        if (peek(OP_MUL) || peek(OP_DIV) || peek(OP_AND)) {
            mul_op();
            factor();
            term_rest();
        }
    }

    private void factor() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE) || peek(LIT_NUMBER)) {
            const_val();
        } else if (peek(OPEN_ROUND)) {
            match(OPEN_ROUND);
            expr();
            match(CLOSE_ROUND);
        } else if (peek(IDENTIFIER)) {
            id_or_func_call();
        } else if (peek(OP_PLUS) || peek(OP_MINUS)) {
            sign();
            factor();
        } else if (peek(OP_NOT)) {
            match(OP_NOT);
            factor();
        }
    }

    private void id_or_func_call() {
        if (peek(IDENTIFIER)) {
            match(IDENTIFIER);
            id_or_func_call_rest();
        }
    }

    private void id_or_func_call_rest() {
        if (peek(OPEN_ROUND)) {
            func_call();
        }
    }

    private void sign() {
        if (peek(OP_PLUS)) {
            match(OP_PLUS);
        } else if (peek(OP_MINUS)) {
            match(OP_MINUS);
        }
    }

    private void mul_op() {
        if (peek(OP_MUL)) {
            match(OP_MUL);
        } else if (peek(OP_DIV)) {
            match(OP_DIV);
        } else if (peek(OP_AND)) {
            match(OP_AND);
        }
    }

    private void add_op() {
        if (peek(OP_PLUS)) {
            match(OP_PLUS);
        } else if (peek(OP_MINUS)) {
            match(OP_MINUS);
        } else if (peek(OP_OR)) {
            match(OP_OR);
        }
    }

    private void rel_op() {
        if (peek(OP_EQ)) match(OP_EQ);
        else if (peek(OP_NEQ)) match(OP_NEQ);
        else if (peek(OP_LT)) match(OP_LT);
        else if (peek(OP_LE)) match(OP_LE);
        else if (peek(OP_GT)) match(OP_GT);
        else if (peek(OP_GE)) match(OP_GE);
    }

    private void const_val() {
        if (peek(CONST_TRUE) || peek(CONST_FALSE)) {
            bool_const();
        } else if (peek(LIT_NUMBER)) {
            number();
        }
    }

    private void number() {
        if (peek(LIT_NUMBER)) {
            match(LIT_NUMBER);
        }
    }

    private void bool_const() {
        if (peek(CONST_TRUE)) match(CONST_TRUE);
        else if (peek(CONST_FALSE)) match(CONST_FALSE);
    }


    public static void main(String[] argv) throws FileNotFoundException {
        if (argv.length == 0) {
            System.out.println("Please provide a file");
            System.exit(0);
        }

        InputStreamReader reader = new InputStreamReader(new FileInputStream(argv[0]));
        new Parser(new Lexer(reader)).parse();
    }
}
