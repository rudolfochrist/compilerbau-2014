import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class Parser {

    private Lexer scanner;

    public Parser(Lexer scanner) {
        this.scanner = scanner;
    }

    private boolean match(String token) {

    }

    public void parse() {

    }

    private void program() {}
    private void decl_part() {}
    private void decl_part_rest() {}
    private void decl_part_func() {}
    private void decl_part_func_rest() {}
    private void type_id() {}
    private void var_decl() {}
    private void var_decl_rest() {}
    private void type() {}
    private void id_list() {}
    private void func_decl() {}
    private void params() {}
    private void param_list() {}
    private void param_list_rest() {}
    private void body() {}
    private void stmt_seq() {}
    private void stmt() {}
    private void simple_stmt() {}
    private void assignment_or_func_call() {}
    private void assignment_or_func_call_rest() {}
    private void struct_stmt() {}
    private void assignment() {}
    private void cond() {}
    private void cond_rest() {}
    private void loop() {}
    private void func_call() {}
    private void args() {}
    private void arg_list() {}
    private void arg_list_rest() {}
    private void return_stmt() {}
    private void comp_stmt() {}
    private void expr() {}
    private void expr_rest() {}
    private void simple_expr() {}
    private void simple_expr_rest() {}
    private void term() {}
    private void term_rest() {}
    private void factor() {}
    private void id_or_func_call() {}
    private void id_or_func_call_rest() {}
    private void sign() {}
    private void mul_op() {}
    private void add_op() {}
    private void rel_op() {}
    private void const_val() {}
    private void number() {}
    private void bool_const() {}


    public static void main(String[] argv) throws FileNotFoundException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(argv[0]));
        new Parser(new Lexer(reader)).parse();
    }
}
