


public class Yytoken {
    private final Parser.Types type;
    private int ival;
    private double dval;
    private String sval;
    private Object oval;

    public Yytoken(Parser.Types type) {
        this.type = type;
    }

    public Yytoken(Parser.Types type, int ival) {
        this.type = type;
        this.ival = ival;
    }

    public Yytoken(Parser.Types type, double dval) {
        this.type = type;
        this.dval = dval;
    }

    public Yytoken(Parser.Types type, String sval) {
        this.type = type;
        this.sval = sval;
    }

    public Yytoken(Parser.Types type, Object oval) {
        this.type = type;
        this.oval = oval;
    }

    public String value() {
        if (sval != null) {
            return sval;
        }
        if (oval != null) {
            return oval.toString();
        }
        if (ival != 0) {
            return ""+ival;
        }
        return ""+dval;
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
        return "Token with type "+type + " and value "+value();
    }
}
