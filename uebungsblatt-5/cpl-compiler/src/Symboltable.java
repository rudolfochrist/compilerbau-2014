import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Map that contains all parameter definitions for each function
     */
    private final Map<String, List<Symbol>> functionparameters;

    public Symboltable() {
        symbols = new HashMap<String, Symbol>();
        functions = new HashMap<String, Map<String, Symbol>>();
        functionparameters = new HashMap<String, List<Symbol>>();
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

    public void addFunctionparameter(Parsercontext context) {
        final String functionname = context.currentScope;
        final Symbol symbol = getContextMap(context.currentScope).get(context.lastFoundIdentifier.value());
        getFunctionparameters(functionname).add(symbol);
    }

    private List<Symbol> getFunctionparameters(String functionname) {
        List<Symbol> list = functionparameters.get(functionname);
        if (list == null) {
            list = new ArrayList<Symbol>();
            functionparameters.put(functionname, list);
        }
        return list;
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
            throw new SymbolException("Identifier '"+identifier.value()+"' cannot be used here because is not a variable name.");
        }
    }

    public void verifyFunctionWasDeclared(Yytoken identifier) throws SymbolException {
        final Symbol symbol = getContextMap(null).get(identifier.value());
        if (symbol == null) {
            throw new SymbolException("Identifier '"+identifier.value()+"' was not declared.");
        }

        if (!symbol.isFunction()) {
            throw new SymbolException("Identifier '"+identifier.value()+"' cannot be used here because it is not a function name.");
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

    public Parser.Types getIdentifierType(Yytoken lastFoundIdentifier, String scope) {
        final String identifierName = lastFoundIdentifier.value();
        final Map<String, Symbol> contextMap = getContextMap(scope);
        final Symbol symbol = contextMap.get(identifierName);
        if (symbol == null) {
            return null;
        }
        return symbol.getIdentifierType();
    }

    public Parser.Types getFunctionType(String functionName) {
        return symbols.get(functionName).getIdentifierType();
    }

    public void verifyFunctionParamCount(String functionname, int i) {
        final int size = getFunctionparameters(functionname).size();
        if (size != i) {
            throw new SymbolException("Function '"+functionname+"' takes "+size+" parameters, but "+i+" were specified.");
        }
    }

    public Parser.Types getFunctionParamType(Metainfo metainfo) {

        final List<Symbol> parameters = getFunctionparameters(metainfo.currentlyCalledFunction);

       if (metainfo.currentlyCalledFunctionParameterCount >= parameters.size()) {
           return null;
       }

        return getFunctionparameters(metainfo.currentlyCalledFunction).get(metainfo.currentlyCalledFunctionParameterCount).getIdentifierType();
    }
}
