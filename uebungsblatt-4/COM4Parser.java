
public class COM4Parser {

    public static final String ZERO_SYM = "0";
    public static final String ONE_SYM= "1";
    public static final String DOT_SYM = ".";
    public static final String EOI = "$";
    private String input;

    private boolean match(String symbol) {
        if (input.startsWith(symbol)) {
            input = input.substring(1);
            return true;
        }
        throw new RuntimeException(String.format("Cannot match symbol %s against %s", symbol, input));
    }

    private int B() {
        if (input.startsWith(ZERO_SYM)) {
            match(ZERO_SYM);
            return 0;
        } else if (input.startsWith(ONE_SYM)) {
            match(ONE_SYM);
            return 1;
        } else {
            throw new RuntimeException(String.format("Cannot parse %s", input));
        }
    }

    private double LDash(String side) {
        if (input.startsWith(ZERO_SYM)) {
            int Bval = B();
            double LDashval = LDash(side);
            if ("R".equals(side)) {
                return (LDashval / 2) + Bval;
            } else {
                return (LDashval * 2) + Bval;
            }
        } else if (input.startsWith(ONE_SYM)) {
            int Bval = B();
            double LDashval = LDash(side);
            if ("R".equals(side)) {
                return (LDashval / 2) + Bval;
            } else {
                return (LDashval * 2) + Bval;
            }
        }
        // else epsilon transion
        return 0;
    }

    private double L(String side) {
        if (input.startsWith(ZERO_SYM)) {
            int Bval = B();
            double LDashval = LDash(side);
            if ("R".equals(side)) {
                return (LDashval / 2) + Bval;
            } else {
                return (LDashval * 2) + Bval;
            }
        } else if (input.startsWith(ONE_SYM)) {
            int Bval = B();
            double LDashval = LDash(side);
            if ("R".equals(side)) {
                return (LDashval / 2) + Bval;
            } else {
                return (LDashval * 2) + Bval;
            }
        } else {
            throw new RuntimeException(String.format("Cannot parse %s", input));
        }
    }

    private double SDash() {
        if (input.startsWith(DOT_SYM)) {
            match(DOT_SYM);
            double Lval = L("R");
            return Lval / 2;
        } else if (input.startsWith(EOI)) {
            return 0;
        } else {
            throw new RuntimeException(String.format("Cannot parse %s", input));
        }
    }

    private double S() {
        if (input.startsWith(ZERO_SYM)) {
            double Lval = L("L");
            double SDashval = SDash();
            return Lval + SDashval;
        } else if (input.startsWith(ONE_SYM)) {
            double Lval = L("L");
            double SDashval = SDash();
            return Lval + SDashval;
        } else {
            throw new RuntimeException(String.format("Cannot parse %s", input));
        }
    }

    public void parse(String input) {
        this.input = input + EOI;
        double Sval = S();
        System.out.println(String.format("Result: %s -> %f", input, Sval));
    }

    public static void main(String[] args) {
        COM4Parser parser = new COM4Parser();
        parser.parse("101.101");
        parser.parse("11.011");
    }
}
