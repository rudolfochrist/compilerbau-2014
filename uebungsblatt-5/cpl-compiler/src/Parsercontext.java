/**
 * Contains some contextual information of the current parser state.
 */
public class Parsercontext {

    /** variable/function type that was last found by the scanner */
    public Yytoken lastFoundType = null;
    /** variable/function identifier(name) that was last found by the scanner */
    public Yytoken lastFoundIdentifier = null;
    /**
     * Current variable scope.
     * Is set to the name of the function that is parsed at the moment.
     * If null, no function is parsed (global scope).
     */
    public String currentScope = null;
}
