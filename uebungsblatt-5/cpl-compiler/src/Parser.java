import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Parser {

    public static enum Types {
        TYPE_INT("int"),
        TYPE_BOOL("bool"),
        OPEN_ROUND ("("),
        COMMA(","),
        SEMICOLON(";"),
        CLOSE_ROUND(")"),
        OPEN_BRACE("{"),
        CLOSE_BRACE("}"),
        KEYWORD_RETURN("return"),
        KEYWORD_IF("if"),
        KEYWORD_WHILE("while"),
        OP_ASSIGNMENT("("),
        KEYWORD_THEN("then"),
        KEYWORD_ENDIF("fi"),
        CONST_TRUE("true"),
        CONST_FALSE("false"),
        OP_PLUS("+"),
        OP_MINUS("-"),
        LIT_NUMBER("lit_number"),
        IDENTIFIER("identifier"),
        OP_EQ("="),
        OP_NEQ("!="),
        OP_LT("<"),
        OP_LE("("),
        OP_GT(">"),
        OP_GE("("),
        OP_MUL("*"),
        OP_DIV("/"),
        OP_AND("and"),
        OP_OR("or"),
        OP_NOT("not"),
        KEYWORD_ELSE("else");

        private final String value;

        Types(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }

    private final Lexer scanner;
    private final Symboltable symbols;
    private final Parsercontext context;
    private Yytoken currentToken;

    public Parser(Lexer scanner) {
        this.scanner = scanner;
        symbols = new Symboltable();
        context = new Parsercontext();
    }

    private boolean match(Types tokenType) {
        if (new Yytoken(tokenType).equals(currentToken)) {
            try {
                currentToken = scanner.yylex();
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean peek(Types tokenType) {
        return new Yytoken(tokenType).equals(currentToken);
    }

    public void parse() {
        try {
            currentToken = scanner.yylex();
            program();
            // System.out.println(symbols); // Print all found symbols (for debug)
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void program() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            decl_part();
        }
    }

    private void decl_part() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            decl_part_rest();
        }
    }

    private void decl_part_rest() {
        if (peek(Types.OPEN_ROUND)) {
            decl_part_func();
        } else if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            var_decl();
            decl_part_func();
        }
    }

    private void decl_part_func() {
        if (peek(Types.OPEN_ROUND)) {
            symbols.addFunction(context);
            context.currentScope = context.lastFoundIdentifier.value();
            func_decl();
            decl_part_func_rest();
        }
    }

    private void decl_part_func_rest() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            decl_part_func();
        }
    }

    private void type_id() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type();
            id();
        }
    }

    private void var_decl() {
        if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            id_list();
            symbols.addVariable(context);
            match(Types.SEMICOLON);
            type_id();
            var_decl_rest();
        }
    }
    private void var_decl_rest() {
        if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            var_decl();
        }
    }

    private void type() {
        final Yytoken token = currentToken;
        if (peek(Types.TYPE_INT)) {
            if (match(Types.TYPE_INT)) {
                context.lastFoundType = token;
            }
        } else if (peek(Types.TYPE_BOOL)) {
            if (match(Types.TYPE_BOOL)) {
                context.lastFoundType = token;
            }
        }
    }

    private void id_list() {
        if (peek(Types.COMMA)) {
            symbols.addVariable(context);
            match(Types.COMMA);
            id();
            id_list();
        }
    }

    private void func_decl() {
        if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);
            params();
            match(Types.CLOSE_ROUND);
            body();
        }
    }

    private void params() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            param_list();
        }
    }

    private void param_list() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            symbols.addVariable(context);
            param_list_rest();
        }
    }

    private void param_list_rest() {
        if (peek(Types.COMMA)) {
            match(Types.COMMA);
            param_list();
        }
    }

    private void body() {
        if (peek(Types.OPEN_BRACE)) {
            match(Types.OPEN_BRACE);
            body_rest();
            match(Types.CLOSE_BRACE);
        }
    }

    private void body_rest() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) ||
                peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            stmt_seq();
        } else if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            var_decl_body();
            stmt_seq();
        }
    }

    private void var_decl_body() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            id_list();
            symbols.addVariable(context);
            match(Types.SEMICOLON);
            var_decl_body();
        }
    }

    private void stmt_seq() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) ||
                peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            stmt();
            stmt_seq();
        }
    }

    private void stmt() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN)) {
            simple_stmt();
            match(Types.SEMICOLON);
        } else if (peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            struct_stmt();
        }
    }

    private void simple_stmt() {
        if (peek(Types.IDENTIFIER)) {
            assignment_or_func_call();
        } else if (peek(Types.KEYWORD_RETURN)) {
            return_stmt();
        }
    }

    private void assignment_or_func_call() {
        if (peek(Types.IDENTIFIER)) {
            id();
            assignment_or_func_call_rest();
        }
    }

    private void id() {
        final Yytoken token = currentToken;
        if(match(Types.IDENTIFIER)) {
            context.lastFoundIdentifier = token;
        }
    }

    private void assignment_or_func_call_rest() {
        if (peek(Types.OP_ASSIGNMENT)) {
            assignment();
        }
    }

    private void struct_stmt() {
        if (peek(Types.OPEN_BRACE)) {
            comp_stmt();
        } else if (peek(Types.KEYWORD_IF)) {
            cond();
        } else if (peek(Types.KEYWORD_WHILE)) {
            loop();
        }
    }

    private void assignment() {
        if (peek(Types.OP_ASSIGNMENT)) {
            match(Types.OP_ASSIGNMENT);
            expr();
        }
    }

    private void cond() {
        if (peek(Types.KEYWORD_IF)) {
            match(Types.KEYWORD_IF);
            match(Types.OPEN_ROUND);
            expr();
            match(Types.CLOSE_ROUND);
            match(Types.KEYWORD_THEN);
            stmt();
            cond_rest();
        }
    }

    private void cond_rest() {
        if (peek(Types.KEYWORD_ENDIF)) {
            match(Types.KEYWORD_ENDIF);
        } else if (peek(Types.KEYWORD_ELSE)) {
            match(Types.KEYWORD_ELSE);
            stmt();
            match(Types.KEYWORD_ENDIF);
        }
    }

    private void loop() {
        if (peek(Types.KEYWORD_WHILE)) {
            match(Types.KEYWORD_WHILE);
            match(Types.OPEN_ROUND);
            expr();
            match(Types.CLOSE_ROUND);
            stmt();
        }
    }

    private void func_call() {
        if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);
            args();
            match(Types.CLOSE_ROUND);
        }
    }

    private void args() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            arg_list();
        }
    }

    private void arg_list() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            expr();
            arg_list_rest();
        }
    }

    private void arg_list_rest() {
        if (peek(Types.COMMA)) {
            match(Types.COMMA);
            arg_list();
        }
    }

    private void return_stmt() {
        if (peek(Types.KEYWORD_RETURN)) {
            match(Types.KEYWORD_RETURN);
            expr();
        }
    }

    private void comp_stmt() {
        if (peek(Types.OPEN_BRACE)) {
            match(Types.OPEN_BRACE);
            stmt_seq();
            match(Types.CLOSE_BRACE);
        }
    }

    private void expr() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            simple_expr();
            expr_rest();
        }
    }

    private void expr_rest() {
        if (peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) ||
                peek(Types.OP_LE) || peek(Types.OP_GT) || peek(Types.OP_GE)) {
            rel_op();
            simple_expr();
        }
    }

    private void simple_expr() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            term();
            simple_expr_rest();
        }
    }

    private void simple_expr_rest() {
        if (peek(Types.OP_PLUS) || peek(Types.OP_MINUS) || peek(Types.OP_OR)) {
            add_op();
            term();
            simple_expr_rest();
        }
    }

    private void term() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            factor();
            term_rest();
        }
    }

    private void term_rest() {
        if (peek(Types.OP_MUL) || peek(Types.OP_DIV) || peek(Types.OP_AND)) {
            mul_op();
            factor();
            term_rest();
        }
    }

    private void factor() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER)) {
            const_val();
        } else if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);
            expr();
            match(Types.CLOSE_ROUND);
        } else if (peek(Types.IDENTIFIER)) {
            id_or_func_call();
        } else if (peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sign();
            factor();
        } else if (peek(Types.OP_NOT)) {
            match(Types.OP_NOT);
            factor();
        }
    }

    private void id_or_func_call() {
        if (peek(Types.IDENTIFIER)) {
            id();
            id_or_func_call_rest();
        }
    }

    private void id_or_func_call_rest() {
        if (peek(Types.OPEN_ROUND)) {
            func_call();
        }
    }

    private void sign() {
        if (peek(Types.OP_PLUS)) {
            match(Types.OP_PLUS);
        } else if (peek(Types.OP_MINUS)) {
            match(Types.OP_MINUS);
        }
    }

    private void mul_op() {
        if (peek(Types.OP_MUL)) {
            match(Types.OP_MUL);
        } else if (peek(Types.OP_DIV)) {
            match(Types.OP_DIV);
        } else if (peek(Types.OP_AND)) {
            match(Types.OP_AND);
        }
    }

    private void add_op() {
        if (peek(Types.OP_PLUS)) {
            match(Types.OP_PLUS);
        } else if (peek(Types.OP_MINUS)) {
            match(Types.OP_MINUS);
        } else if (peek(Types.OP_OR)) {
            match(Types.OP_OR);
        }
    }

    private void rel_op() {
        if (peek(Types.OP_EQ)) {
            match(Types.OP_EQ);
        } else if (peek(Types.OP_NEQ)) {
            match(Types.OP_NEQ);
        } else if (peek(Types.OP_LT)) {
            match(Types.OP_LT);
        } else if (peek(Types.OP_LE)) {
            match(Types.OP_LE);
        } else if (peek(Types.OP_GT)) {
            match(Types.OP_GT);
        } else if (peek(Types.OP_GE)) {
            match(Types.OP_GE);
        }
    }

    private void const_val() {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE)) {
            bool_const();
        } else if (peek(Types.LIT_NUMBER)) {
            number();
        }
    }

    private void number() {
        if (peek(Types.LIT_NUMBER)) {
            match(Types.LIT_NUMBER);
        }
    }

    private void bool_const() {
        if (peek(Types.CONST_TRUE)) {
            match(Types.CONST_TRUE);
        } else if (peek(Types.CONST_FALSE)) {
            match(Types.CONST_FALSE);
        }
    }

    private void addParserWarning(Yytoken actual, Types... expected) {
        StringBuffer sb = new StringBuffer();
        sb.append("PARSER WARNING: ");
        sb.append("Expected on of ");
        for (int i = 0; i < expected.length; i++) {
            Types t = expected[i];
            sb.append(t.value());
            if (i < expected.length - 1) {
                sb.append(", ");
            }
        }
        sb.append( " but got ");
        sb.append(actual.value());
        sb.append(".");
    }


    public static void main(String[] argv) throws IOException {
        if (argv.length == 0) {
            System.out.println("Please provide a file");
            System.exit(0);
        }

        final InputStreamReader reader = new InputStreamReader(new FileInputStream(argv[0]));
        final String filename = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime())
                + "-cpl-compiler-listing.txt";
        final PrintWriter writer = new PrintWriter(filename, "UTF-8");
        new Parser(new Lexer(reader, writer)).parse();
        reader.close();
        writer.close();
    }
}
