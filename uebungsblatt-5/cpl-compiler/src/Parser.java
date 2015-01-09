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
        OP_ASSIGNMENT(":="),
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
                final Yytoken lastToken = currentToken;
                currentToken = scanner.yylex();
                context.lastFoundToken = lastToken;
                return true;
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        throw new ParserException(String.format("PARSER ERROR: Tried to match %s but was %s\nAborting...", tokenType.value(), currentToken.getType().value()));
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
        } else if (currentToken == null) {
            // null == $
            //sync(); // EPSILON
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void decl_part() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            decl_part_rest();
        } else if (currentToken == null) {
            sync();
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void decl_part_rest() {
        if (peek(Types.OPEN_ROUND)) {
            decl_part_func();
        } else if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            var_decl();
            decl_part_func();
        } else if (currentToken == null) {
            sync();
        } else {
            throwParseError(Types.OPEN_ROUND, Types.COMMA, Types.SEMICOLON);
        }
    }

    private void decl_part_func() {
        if (peek(Types.OPEN_ROUND)) {
            try {
                symbols.addFunction(context);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if symbol name already exists ?
            }
            context.currentScope = context.lastFoundIdentifier.value();
            func_decl();
            decl_part_func_rest();
        } else if (currentToken == null) {
            sync();
        } else {
            throwParseError(Types.OPEN_ROUND);
        }
    }

    private void decl_part_func_rest() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            decl_part_func();
        } else if (currentToken == null) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void type_id() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type();
            id(new Metainfo());
        } else if (peek(Types.OPEN_ROUND) || peek(Types.COMMA)) {
            sync();
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void var_decl() {
        if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            id_list();
            try {
                symbols.addVariable(context);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if symbol name already exists ?
            }
            match(Types.SEMICOLON);
            type_id();
            var_decl_rest();
        } else if (peek(Types.OPEN_ROUND)) {
            sync();
        } else {
            throwParseError(Types.COMMA, Types.SEMICOLON);
        }
    }

    private void var_decl_rest() {
        if (peek(Types.COMMA) || peek(Types.SEMICOLON)) {
            var_decl();
        } else if (peek(Types.OPEN_ROUND)) {
            //sync(); EPSILON
        } else {
            throwParseError(Types.COMMA, Types.SEMICOLON);
        }
    }

    private void type() {
        if (peek(Types.TYPE_INT)) {
            if (match(Types.TYPE_INT)) {
                context.lastFoundType = context.lastFoundToken;
            }
        } else if (peek(Types.TYPE_BOOL)) {
            if (match(Types.TYPE_BOOL)) {
                context.lastFoundType = context.lastFoundToken;
            }
        } else if (peek(Types.IDENTIFIER)) {
            sync();
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void id_list() {
        if (peek(Types.COMMA)) {
            try {
                symbols.addVariable(context);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if symbol name already exists ?
            }
            match(Types.COMMA);
            id(new Metainfo());
            id_list();
        } else if (peek(Types.SEMICOLON)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.COMMA);
        }
    }

    private void func_decl() {
        if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);
            params();
            match(Types.CLOSE_ROUND);
            body();
        } else if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL) || currentToken == null) {
            sync();
        } else {
            throwParseError(Types.OPEN_ROUND);
        }
    }

    private void params() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            param_list();
        } else if (peek(Types.CLOSE_ROUND)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void param_list() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            try {
                symbols.addVariable(context);
                symbols.addFunctionparameter(context);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if symbol name already exists ?
            }
            param_list_rest();
        } else if (peek(Types.CLOSE_ROUND)) {
            sync();
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void param_list_rest() {
        if (peek(Types.COMMA)) {
            match(Types.COMMA);
            param_list();
        } else if (peek(Types.CLOSE_ROUND)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.COMMA);
        }
    }

    private void body() {
        if (peek(Types.OPEN_BRACE)) {
            match(Types.OPEN_BRACE);
            body_rest();
            match(Types.CLOSE_BRACE);
        } else if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL) || currentToken == null) {
            sync();
        } else {
            throwParseError(Types.OPEN_BRACE);
        }
    }

    private void body_rest() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) ||
                peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            stmt_seq();
        } else if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            var_decl_body();
            stmt_seq();
        } else if (peek(Types.CLOSE_BRACE)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.IDENTIFIER, Types.KEYWORD_RETURN, Types.OPEN_BRACE, Types.KEYWORD_IF, Types.KEYWORD_WHILE, Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void var_decl_body() {
        if (peek(Types.TYPE_INT) || peek(Types.TYPE_BOOL)) {
            type_id();
            id_list();
            try {
                symbols.addVariable(context);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if symbol name already exists ?
            }
            match(Types.SEMICOLON);
            var_decl_body();
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE) || peek(Types.CLOSE_BRACE)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.TYPE_INT, Types.TYPE_BOOL);
        }
    }

    private void stmt_seq() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) ||
                peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            stmt();
            stmt_seq();
        } else if (peek(Types.CLOSE_BRACE)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.IDENTIFIER, Types.KEYWORD_RETURN, Types.OPEN_BRACE, Types.KEYWORD_IF, Types.KEYWORD_WHILE);
        }
    }

    private void stmt() {
        if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN)) {
            simple_stmt();
            match(Types.SEMICOLON);
        } else if (peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE)) {
            struct_stmt();
        } else if (peek(Types.CLOSE_BRACE) || peek(Types.KEYWORD_ENDIF) || peek(Types.KEYWORD_ELSE)) {
            sync();
        } else {
            throwParseError(Types.IDENTIFIER, Types.KEYWORD_RETURN, Types.OPEN_BRACE, Types.KEYWORD_IF, Types.KEYWORD_WHILE);
        }
    }

    private void simple_stmt() {
        if (peek(Types.IDENTIFIER)) {
            assignment_or_func_call();
        } else if (peek(Types.KEYWORD_RETURN)) {
            return_stmt();
        } else if (peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.IDENTIFIER, Types.KEYWORD_RETURN);
        }
    }

    private void assignment_or_func_call() {
        if (peek(Types.IDENTIFIER)) {
            id(new Metainfo());
            assignment_or_func_call_rest();
        } else if (peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.IDENTIFIER);
        }
    }

    private Metainfo id(Metainfo metainfo) {
        if(match(Types.IDENTIFIER)) {
            context.lastFoundIdentifier = context.lastFoundToken;
            final Types identifierType = symbols.getIdentifierType(context.lastFoundIdentifier, context.currentScope);
            checkMatchingTypes(metainfo, identifierType);
            return metainfo.expectedType == null ? new Metainfo(identifierType) : metainfo;
        }
        return metainfo;
    }


    private void assignment_or_func_call_rest() {
        if (peek(Types.OP_ASSIGNMENT)) {
            assignment();
        } else if (peek(Types.OPEN_ROUND)) {
            func_call();
        } else if (peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.OP_ASSIGNMENT, Types.OPEN_ROUND);
        }
    }

    private void struct_stmt() {
        if (peek(Types.OPEN_BRACE)) {
            comp_stmt();
        } else if (peek(Types.KEYWORD_IF)) {
            cond();
        } else if (peek(Types.KEYWORD_WHILE)) {
            loop();
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.CLOSE_BRACE) || peek(Types.KEYWORD_ENDIF) || peek(Types.KEYWORD_ELSE)) {
            sync();
        } else {
            throwParseError(Types.OPEN_BRACE, Types.KEYWORD_IF, Types.KEYWORD_WHILE);
        }
    }

    private void assignment() {
        if (peek(Types.OP_ASSIGNMENT)) {
            match(Types.OP_ASSIGNMENT);
            try {
                symbols.verifyVariableWasDeclared(context.lastFoundIdentifier, context.currentScope);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if this occurs
            }
            final Metainfo metainfo = expr(new Metainfo());
            final Types expectedType = symbols.getIdentifierType(context.lastFoundIdentifier, context.currentScope); // Type of the left hand side of the assignment
            checkMatchingTypes(expectedType, metainfo.expectedType); // check if left and right side of the assignment have the same type

        } else if (peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.OP_ASSIGNMENT);
        }
    }

    private void cond() {
        if (peek(Types.KEYWORD_IF)) {
            match(Types.KEYWORD_IF);
            match(Types.OPEN_ROUND);

            final Metainfo metainfo = expr(new Metainfo());
            checkMatchingTypes(metainfo.expectedType, Types.TYPE_BOOL); // check if condition has bool return type

            match(Types.CLOSE_ROUND);
            match(Types.KEYWORD_THEN);
            stmt();
            cond_rest();
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_WHILE) || peek(Types.CLOSE_BRACE) || peek(Types.KEYWORD_ENDIF) || peek(Types.KEYWORD_ELSE)) {
            sync();
        } else {
            throwParseError(Types.KEYWORD_IF);
        }
    }

    private void cond_rest() {
        if (peek(Types.KEYWORD_ENDIF)) {
            match(Types.KEYWORD_ENDIF);
        } else if (peek(Types.KEYWORD_ELSE)) {
            match(Types.KEYWORD_ELSE);
            stmt();
            match(Types.KEYWORD_ENDIF);
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_WHILE) || peek(Types.CLOSE_BRACE)) {
            sync();
        } else {
            throwParseError(Types.KEYWORD_ENDIF, Types.KEYWORD_ELSE);
        }
    }

    private void loop() {
        if (peek(Types.KEYWORD_WHILE)) {
            match(Types.KEYWORD_WHILE);
            match(Types.OPEN_ROUND);

            final Metainfo metainfo = expr(new Metainfo());
            checkMatchingTypes(metainfo.expectedType, Types.TYPE_BOOL); // check if loop condition has bool return type

            match(Types.CLOSE_ROUND);
            stmt();
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.OPEN_BRACE) || peek(Types.KEYWORD_IF) || peek(Types.CLOSE_BRACE) || peek(Types.KEYWORD_ENDIF) || peek(Types.KEYWORD_ELSE)) {
            sync();
        } else {
            throwParseError(Types.KEYWORD_WHILE);
        }
    }

    private void func_call() {
        if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);

            try {
                symbols.verifyFunctionWasDeclared(context.lastFoundIdentifier);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if this occurs
            }

            args();
            match(Types.CLOSE_ROUND);
        } else if (peek(Types.OP_MUL) || peek(Types.OP_DIV) || peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) || peek(Types.OP_OR) ||
                peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) || peek(Types.OP_GE) ||
                peek(Types.COMMA) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.OPEN_ROUND);
        }
    }

    private void args() {
        final Metainfo metainfo = new Metainfo();
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            metainfo.currentlyCalledFunction = context.lastFoundIdentifier.value(); // save the name of the currently parsed function for parameter validation
            arg_list(metainfo);
        } else if (peek(Types.CLOSE_ROUND)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.IDENTIFIER, Types.OPEN_ROUND, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT, Types.LIT_NUMBER);
        }

        try {
            symbols.verifyFunctionParamCount(metainfo.currentlyCalledFunction, metainfo.currentlyCalledFunctionParameterCount); // check if the right number of parameters were passed to the function
        } catch (final Symboltable.SymbolException e) {
            scanner.messages.error(scanner.lineno, e.getMessage());
        }
    }

    private void arg_list(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {

            final Types paramType = symbols.getFunctionParamType(metainfo);
            expr(new Metainfo(paramType)); // expression must have the type that was declared in the function definition
            metainfo.currentlyCalledFunctionParameterCount++;
            arg_list_rest(metainfo);
        } else if (peek(Types.CLOSE_ROUND)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.IDENTIFIER, Types.OPEN_ROUND, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT, Types.LIT_NUMBER);
        }
    }

    private void arg_list_rest(Metainfo metainfo) {
        if (peek(Types.COMMA)) {
            match(Types.COMMA);
            arg_list(metainfo);
        } else if (peek(Types.CLOSE_ROUND)) {
            // sync(); EPSILON
        } else {
            throwParseError(Types.COMMA);
        }
    }

    private void return_stmt() {
        if (peek(Types.KEYWORD_RETURN)) {
            match(Types.KEYWORD_RETURN);
            final Metainfo metainfo = expr(new Metainfo());
            final Types functionType = symbols.getFunctionType(context.currentScope); // scope is always the name of the current function
            // Return expression has to have the declared function type
            checkMatchingTypes(functionType, metainfo.expectedType);

        } else if (peek(Types.SEMICOLON)) {
            sync();
        } else {
            throwParseError(Types.KEYWORD_RETURN);
        }
    }

    private void comp_stmt() {
        if (peek(Types.OPEN_BRACE)) {
            match(Types.OPEN_BRACE);
            stmt_seq();
            match(Types.CLOSE_BRACE);
        } else if (peek(Types.IDENTIFIER) || peek(Types.KEYWORD_RETURN) || peek(Types.KEYWORD_IF) || peek(Types.KEYWORD_WHILE) || peek(Types.CLOSE_BRACE) ||
                peek(Types.KEYWORD_ENDIF) || peek(Types.KEYWORD_ELSE)) {
            sync();
        } else {
            throwParseError(Types.OPEN_BRACE);
        }
    }

    private Metainfo expr(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            final Metainfo metainfoNew = simple_expr(metainfo);
            return expr_rest(metainfoNew);
        } else if (peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.IDENTIFIER, Types.OPEN_ROUND, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT, Types.LIT_NUMBER);
        }
        return new Metainfo();
    }

    private Metainfo expr_rest(Metainfo metainfo) {
        if (peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) ||
                peek(Types.OP_LE) || peek(Types.OP_GT) || peek(Types.OP_GE)) {
            // metainfoNew contains the return type that is required by the rel_op
            // operator = and != do not have a type requirement, the other operators require type int
            final Metainfo metainfoNew = rel_op(metainfo);
            simple_expr(metainfoNew);
            return new Metainfo(Types.TYPE_BOOL); // when a rel_op is used, the resulting return type is always bool!
        } else if (peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA)) {
            return metainfo;
            // sync(); EPSILON
        } else {
            throwParseError(Types.OP_EQ, Types.OP_NEQ, Types.OP_LT, Types.OP_LE, Types.OP_GT, Types.OP_GE);
        }
        return new Metainfo();
    }

    private Metainfo simple_expr(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            final Metainfo metainfoNew = term(metainfo);
            return simple_expr_rest(metainfoNew);
        } else if (peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) || peek(Types.OP_GE) ||
                peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.IDENTIFIER, Types.OPEN_ROUND, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT, Types.LIT_NUMBER);
        }
        return new Metainfo();
    }

    private Metainfo simple_expr_rest(Metainfo metainfo) {
        if (peek(Types.OP_PLUS) || peek(Types.OP_MINUS) || peek(Types.OP_OR)) {
            Metainfo metainfoNew = add_op(metainfo);
            metainfoNew = term(metainfoNew);
            return simple_expr_rest(metainfoNew);
        } else if (peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) || peek(Types.OP_GE) ||
                peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA)) {
            // sync(); EPSILON
            return metainfo;
        } else {
            throwParseError(Types.OP_PLUS, Types.OP_MINUS, Types.OP_OR);
        }
        return new Metainfo();
    }

    private Metainfo term(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.IDENTIFIER) ||
                peek(Types.OPEN_ROUND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS) ||
                peek(Types.OP_NOT) || peek(Types.LIT_NUMBER)) {
            final Metainfo metainfoNew = factor(metainfo);
            return term_rest(metainfoNew);
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.IDENTIFIER, Types.OPEN_ROUND, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT, Types.LIT_NUMBER);
        }
        return new Metainfo();
    }

    private Metainfo term_rest(Metainfo metainfo) {
        if (peek(Types.OP_MUL) || peek(Types.OP_DIV) || peek(Types.OP_AND)) {
            Metainfo metainfoNew = mul_op(metainfo);
            metainfoNew = factor(metainfoNew);
            return term_rest(metainfoNew);
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {

            return metainfo;
            // sync(); EPSILON
        } else {
            throwParseError(Types.OP_MUL, Types.OP_DIV, Types.OP_AND);
        }
        return new Metainfo();
    }

    private Metainfo factor(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER)) {
            return const_val(metainfo);
        } else if (peek(Types.OPEN_ROUND)) {
            match(Types.OPEN_ROUND);
            final Metainfo metainfoNew = expr(metainfo);
            match(Types.CLOSE_ROUND);
            return metainfoNew;
        } else if (peek(Types.IDENTIFIER)) {
            return id_or_func_call(metainfo);
        } else if (peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            final Metainfo metainfoNew = sign(metainfo);
            return factor(metainfoNew);
        } else if (peek(Types.OP_NOT)) {
            match(Types.OP_NOT);
            checkMatchingTypes(metainfo, Types.TYPE_BOOL);
            return factor(metainfo);
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.LIT_NUMBER, Types.OPEN_ROUND, Types.IDENTIFIER, Types.OP_PLUS, Types.OP_MINUS, Types.OP_NOT);
        }
        return metainfo;
    }

    private Metainfo id_or_func_call(Metainfo metainfo) {
        if (peek(Types.IDENTIFIER)) {
            final Metainfo metainfoNew = id(metainfo);
            id_or_func_call_rest();
            return metainfoNew;
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        }  else {
            throwParseError(Types.IDENTIFIER);
        }
        return new Metainfo();
    }

    private void id_or_func_call_rest() {
        if (peek(Types.OPEN_ROUND)) {
            func_call();
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {

            try {
                symbols.verifyVariableWasDeclared(context.lastFoundIdentifier, context.currentScope);
            } catch (final Symboltable.SymbolException e) {
                scanner.messages.error(scanner.lineno, e.getMessage());
                // TODO: error handling if this occurs
            }
            // sync(); EPSILON
        } else {
            throwParseError(Types.OPEN_ROUND);
        }
    }

    private Metainfo sign(Metainfo metainfo) {
        if (peek(Types.OP_PLUS)) {
            match(Types.OP_PLUS);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_MINUS)) {
            match(Types.OP_MINUS);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER) || peek(Types.IDENTIFIER) || peek(Types.OPEN_ROUND) ||
                peek(Types.OP_NOT)) {
            sync();
        } else {
            throwParseError(Types.OP_PLUS, Types.OP_MINUS);
        }
        return metainfo;
    }

    private Metainfo mul_op(Metainfo metainfo) {
        if (peek(Types.OP_MUL)) {
            match(Types.OP_MUL);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_DIV)) {
            match(Types.OP_DIV);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_AND)) {
            match(Types.OP_AND);
            checkMatchingTypes(metainfo, Types.TYPE_BOOL);
        } else if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER) || peek(Types.IDENTIFIER) || peek(Types.OPEN_ROUND) ||
                peek(Types.OP_NOT) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        } else {
            throwParseError(Types.OP_MUL, Types.OP_DIV, Types.OP_AND);
        }
        return metainfo;
    }

    private Metainfo add_op(Metainfo metainfo) {
        if (peek(Types.OP_PLUS)) {
            match(Types.OP_PLUS);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_MINUS)) {
            match(Types.OP_MINUS);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_OR)) {
            match(Types.OP_OR);
            checkMatchingTypes(metainfo, Types.TYPE_BOOL);
        } else if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER) || peek(Types.IDENTIFIER) || peek(Types.OPEN_ROUND) ||
                peek(Types.OP_NOT)) {
            // EPSILON
        } else {
            throwParseError(Types.OP_PLUS, Types.OP_MINUS, Types.OP_OR);
        }
        return metainfo;
    }

    private Metainfo rel_op(Metainfo metainfo) {
        if (peek(Types.OP_EQ)) {
            match(Types.OP_EQ);
        } else if (peek(Types.OP_NEQ)) {
            match(Types.OP_NEQ);
        } else if (peek(Types.OP_LT)) {
            match(Types.OP_LT);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_LE)) {
            match(Types.OP_LE);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_GT)) {
            match(Types.OP_GT);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.OP_GE)) {
            match(Types.OP_GE);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
        } else if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE) || peek(Types.LIT_NUMBER) || peek(Types.IDENTIFIER) || peek(Types.OPEN_ROUND) ||
                peek(Types.OP_NOT) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        } else {
            throwParseError(Types.OP_EQ, Types.OP_NEQ, Types.OP_LT, Types.OP_LE, Types.OP_GT, Types.OP_GE);
        }
        return metainfo;
    }

    private Metainfo const_val(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE) || peek(Types.CONST_FALSE)) {
            return bool_const(metainfo);
        } else if (peek(Types.LIT_NUMBER)) {
            return number(metainfo);
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE, Types.LIT_NUMBER);
        }
        return new Metainfo();
    }

    private Metainfo number(Metainfo metainfo) {
        if (peek(Types.LIT_NUMBER)) {
            match(Types.LIT_NUMBER);
            checkMatchingTypes(metainfo, Types.TYPE_INT);
            return metainfo.expectedType == null ? new Metainfo(Types.TYPE_INT) : metainfo;
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        } else {
            throwParseError(Types.LIT_NUMBER);
        }
        return metainfo;
    }

    private void checkMatchingTypes(Metainfo metainfo, Types actualType) {
        checkMatchingTypes(metainfo.expectedType, actualType);
    }

    private void checkMatchingTypes(Types expectedType, Types actualType) {

        if (actualType == null || expectedType == null) {
            return;
        }

        if (expectedType != actualType) {
            scanner.messages.error(scanner.lineno, "Expected "+expectedType+" but got "+actualType+ " at "+context.lastFoundToken);
        }
    }

    private Metainfo bool_const(Metainfo metainfo) {
        if (peek(Types.CONST_TRUE)) {
            match(Types.CONST_TRUE);
            checkMatchingTypes(metainfo, Types.TYPE_BOOL);
            return metainfo.expectedType == null ? new Metainfo(Types.TYPE_BOOL) : metainfo;
        } else if (peek(Types.CONST_FALSE)) {
            match(Types.CONST_FALSE);
            checkMatchingTypes(metainfo, Types.TYPE_BOOL);
        } else if (peek(Types.OP_OR) || peek(Types.OP_EQ) || peek(Types.OP_NEQ) || peek(Types.OP_LT) || peek(Types.OP_LE) || peek(Types.OP_GT) ||
                peek(Types.OP_GE) || peek(Types.CLOSE_ROUND) || peek(Types.SEMICOLON) || peek(Types.COMMA) || peek(Types.OP_MUL) || peek(Types.OP_DIV) ||
                peek(Types.OP_AND) || peek(Types.OP_PLUS) || peek(Types.OP_MINUS)) {
            sync();
        } else {
            throwParseError(Types.CONST_TRUE, Types.CONST_FALSE);
        }
        return metainfo;
    }

    private void throwParseError(Types... types) {
        throw new ParserException("ERROR: " + parserMessage(types));
    }

    private String parserMessage(Types... expected) {
        final StringBuffer sb = new StringBuffer();
        sb.append("Expected one of [");
        for (int i = 0; i < expected.length; i++) {
            final Types t = expected[i];
            sb.append(t.value());
            if (i < expected.length - 1) {
                sb.append(", ");
            }
        }
        sb.append( "] but got ");
        sb.append(currentToken.getType().value());
        sb.append(".");
        return sb.toString();
    }

    private void sync() {
        final String tokenString = (currentToken != null) ? currentToken.getType().value() : "$";
        scanner.messages.warning(scanner.lineno, String.format("Sync on %s", tokenString));
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
        final Lexer lexer = new Lexer(reader, writer);
        try {
            new Parser(lexer).parse();
            lexer.messages.print(lexer.lineno, writer); // Print messages of last line
        } catch (final ParserException e) {
            // print pending messages
            writer.println(); // Line feed. Exceptions skips unread newlines. Just for aesthetic reasons.
            lexer.messages.print(lexer.lineno, writer);
            writer.println(e.getMessage());
        }
        reader.close();
        writer.close();
    }

    static class ParserException extends RuntimeException {
        public ParserException(String message) {
            super(message);
        }
    }
}
