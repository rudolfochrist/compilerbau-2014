
import java.io.FileReader;
import java.io.BufferedReader;

public class WhitespaceTrimmer {

    private static final String EMPTY_STRING = "";
    private static final int NUL = 0;
    private static final int TAB = 9;
    private static final int SPACE = 32;

    public static boolean isWhitespace(int ch) {
        return (ch == NUL || ch == TAB || ch == SPACE);
    }

    public static boolean isNotWhitespace(int ch) {
        return !isWhitespace(ch);
    }

    public static String trimLeading(String line) {
        for (int i = 0; i < line.length(); i++) {
            int c = line.charAt(i);
            if (isNotWhitespace(c)) {
                return line.substring(i, line.length());
            }
        }
        return EMPTY_STRING;
    }

    public static String trimTrailing(String line) {
        for (int i = line.length()-1; i > 0; i--) {
            int c = line.charAt(i);
            if (isNotWhitespace(c)) {
                return line.substring(0, i+1);
            }
        }

        return EMPTY_STRING;
    }


    public static String trimInside(String line) {
        StringBuffer trimmed = new StringBuffer();

        for (int i = 0; i < line.length(); i++) {
            int c = line.charAt(i);

            if (isWhitespace(c)) {
                char next = line.charAt(i+1);
                if (isNotWhitespace(next)) {
                    trimmed.append((char) c);
                }
            } else {
                trimmed.append((char) c);
            }
        }

        return trimmed.toString();
    }

    public static void main(String[] args) throws Exception {
        FileReader fileReader = new FileReader("com-2-2a-test-script.txt");
        BufferedReader br = new BufferedReader(fileReader);

        String line;
        while ((line = br.readLine()) != null) {
            String noLeading = trimLeading(line);
            String noTrailing = trimTrailing(noLeading);
            String trimmed = trimInside(noTrailing);

            System.out.println(trimmed);
        }
    }
}
