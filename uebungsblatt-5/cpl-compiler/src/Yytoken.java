


public class Yytoken {
    private final Parser.Types type;
    private String sval;

    public Yytoken(Parser.Types type) {
        this.type = type;
    }

    public Yytoken(Parser.Types type, String sval) {
        this.type = type;
        this.sval = sval;
    }

    public String value() {
        return sval;
    }

    public Parser.Types getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Yytoken yytoken = (Yytoken) o;

        if (type != yytoken.type) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        String toString = "Token "+type;
        if (sval != null) {
            toString += " with value "+value();
        }
        return toString;
    }
}
