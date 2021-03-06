
public class Metainfo {

    /** Type int/bool that is expected/required in the current/next expression */
    public Parser.Types requiredType;

    /** name of the function to which a call is parsed at the moment */
    public String currentlyCalledFunction = null;

    /** number of the function parameter  */
    public int currentlyCalledFunctionParameterCount = 0;

    public Metainfo(Parser.Types requiredType) {
        this.requiredType = requiredType;
    }

    public Metainfo() {

    }
}
