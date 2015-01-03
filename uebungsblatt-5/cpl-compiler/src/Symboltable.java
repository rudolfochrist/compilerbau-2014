import java.util.HashMap;
import java.util.Map;

/**
 * Table that contains all identifiers.
 */
public class Symboltable {

    /**
     * Exception that is thrown if the symbol could not be added to the table.
     */
    public class SymbolException extends RuntimeException {

        public SymbolException(String message) {
            super(message);
        }
    }

    /**
     * Map for the global context: Contains function names and global variables.
     */
    private final Map<String, Symbol> symbols;

    /**
     * Map that contains the local function context for each function.
     */
    private final Map<String, Map<String, Symbol>> functions;

    public Symboltable() {
        symbols = new HashMap<String, Symbol>();
        functions = new HashMap<String, Map<String, Symbol>>();
    }

    /**
     * Adds a variable to the symboltable.
     * @param context Context information about the current parser state
     * @throws SymbolException If there is already a symbol with this name in the symbol's context
     */
    public void addVariable(Parsercontext context) throws SymbolException {
        final String variableName = context.lastFoundIdentifier.value();

        checkValidName(context.currentScope, variableName);

        Map<String, Symbol> map;
        if (context.currentScope != null) {
            map = functions.get(context.currentScope);
        } else {
            map = symbols;
        }
        map.put(variableName, Symbol.variable(context.lastFoundIdentifier, context.lastFoundType, context.currentScope));
    }

    /**
     * Adds a function to the symboltable.
     * @param context Context information about the current parser state
     * @throws SymbolException If there is already a symbol with this name in the symbol's context
     */
    public void addFunction(Parsercontext context) throws SymbolException {
        final String functionName = context.lastFoundIdentifier.value();

        checkValidName(null, functionName);

        symbols.put(functionName, Symbol.function(context.lastFoundIdentifier, context.lastFoundType));
        functions.put(functionName, new HashMap<String, Symbol>());
    }

    /**
     * Checks if a name is valid in a given scope.
     * @param scope The scope in which the name is valid
     * @param name Name that should be valid
     * @return
     */
    private void checkValidName(String scope, String name) throws SymbolException {

        final Map<String, Symbol> map = getContextMap(scope);
        if (map.containsKey(name)) {
            throw new SymbolException("Identifier '"+name+"' is invalid because it is already taken.");
        }
    }

    Map<String, Symbol> getContextMap(String scope) {
        if (scope == null) {
            // global scope
            return symbols;
        }

        // return function scope + global scope
        final Map<String, Symbol> result = new HashMap<String, Symbol>(symbols);
        result.putAll(functions.get(scope));
        return result;
    }

    public void verifyVariableWasDeclared(Yytoken identifier, String scope) throws SymbolException {
        final Symbol symbol = getContextMap(scope).get(identifier.value());
        if (symbol == null) {
            throw new SymbolException("Identifier '"+identifier.value()+"' was not declared.");
        }

        if (!symbol.isVariable()) {
            throw new SymbolException("Cannot assign a value to identifier '"+identifier.value()+"' because it is not a variable.");
        }
    }

    @Override
    public String toString() {

        final StringBuilder b = new StringBuilder("Symboltabelle:\n");
        b.append("Global scope:\n");

        for (final String key : symbols.keySet()) {
            b.append(symbols.get(key)+"\n");
        }

        for (final String func : functions.keySet()) {
            b.append("Scope of function '"+func+"'\n");
            final Map<String, Symbol> map = functions.get(func);
            for (final String key : map.keySet()) {
                b.append(map.get(key)+"\n");
            }
        }

        return b.toString();
    }
}
