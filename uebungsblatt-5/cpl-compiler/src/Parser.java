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

    public static void main(String[] argv) throws FileNotFoundException {
        InputStreamReader reader = new InputStreamReader(new FileInputStream(argv[0]));
        new Parser(new Lexer(reader)).parse();
    }
}
