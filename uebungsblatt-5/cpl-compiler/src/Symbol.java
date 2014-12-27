
public class Symbol {

    public enum Symboltype {
        VARIABLE,
        FUNCTION;
    }

    /** Token that describes the symbol name */
    private final Yytoken identifier;

    /** Token that describes the symbol type (bool/int) */
    private final Yytoken identifiertype;

    /** Type of the symbol (function/variable) */
    private Symboltype symboltype;

    /** Scope of the symbol (name of the function in which it is valid, null = global) */
    private final String scope;

    public static Symbol variable(Yytoken identifier, Yytoken type, String scope) {
        return new Symbol(identifier, type, Symboltype.VARIABLE, scope);
    }

    public static Symbol function(Yytoken identifier, Yytoken type) {
        return new Symbol(identifier, type, Symboltype.FUNCTION);
    }

    private Symbol(Yytoken identifier, Yytoken identifiertype, Symboltype symboltype, String scope) {
        this.identifier = identifier;
        this.identifiertype = identifiertype;
        this.symboltype = symboltype;
        this.scope = scope;
    }

    private Symbol(Yytoken identifier, Yytoken identifiertype, Symboltype symboltype) {
        this.identifier = identifier;
        this.identifiertype = identifiertype;
        this.symboltype = symboltype;
        this.scope = null;
    }

    public String scope() {
        return scope;
    }

    public Symbol setSymboltype(Symboltype symboltype) {
        this.symboltype = symboltype;
        return this;
    }

    public String getIdentifierName() {
        return identifier.value();
    }

    @Override
    public String toString() {
        return "Symbol: Identifier "+identifier.value()+", "+identifiertype.getType()+", "+symboltype+", scope: "+scope;
    }
}
