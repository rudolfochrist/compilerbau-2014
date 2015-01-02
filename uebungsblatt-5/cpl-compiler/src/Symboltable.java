import java.util.HashMap;
import java.util.Map;

/**
 * Table that contains all identifiers.
 */
public class Symboltable {

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
     */
    public void addVariable(Parsercontext context) {
        final String variableName = context.lastFoundIdentifier.value();

        if (validName(context.currentScope, variableName)) {
            Map<String, Symbol> map;
            if (context.currentScope != null) {
                map = functions.get(context.currentScope);
            } else {
                map = symbols;
            }
            map.put(variableName, Symbol.variable(context.lastFoundIdentifier, context.lastFoundType, context.currentScope));
        }
    }

    /**
     * Adds a function to the symboltable.
     * @param context Context information about the current parser state
     */
    public void addFunction(Parsercontext context) {
        final String functionName = context.lastFoundIdentifier.value();

        if (validName(null, functionName)) {
            symbols.put(functionName, Symbol.function(context.lastFoundIdentifier, context.lastFoundType));
            functions.put(functionName, new HashMap<String, Symbol>());
        }
    }

    /**
     * Checks if a name is valid in a given scope.
     * @param scope The scope in which the name is valid
     * @param name Name that should be valid
     * @return
     */
    private boolean validName(String scope, String name) {

        Map<String, Symbol> map;
        if (scope != null) {
            // current scope is a function
            map = new HashMap<>(functions.get(scope));
            // add global symbols to temporary map to check if the local identifier is already declared as global identifier
            map.putAll(symbols);
        } else {
            // current scope is global scope
            map = symbols;
        }

        if (map.containsKey(name)) {
            System.out.println("Identifier '"+name+"' is already taken!");
            return false;
        }

        return true;
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
