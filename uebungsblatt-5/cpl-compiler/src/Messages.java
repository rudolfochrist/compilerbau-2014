import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Messages {

    private enum Type {
        WARNING,
        ERROR;

        @Override
        public String toString() {
            return super.toString()+": ";
        }
    }

    private final Map<Integer, List<String>> messages = new HashMap<Integer, List<String>>();

    public void warning(int line, String text) {
        getList(line).add(Type.WARNING+text+"\n");
    }

    public void error(int line, String text) {
        getList(line).add(Type.ERROR+text+"\n");
    }

    private List<String> getList(int line) {
        List<String> list = messages.get(line);
        if (list == null) {
            list = new ArrayList<String>();
            messages.put(line, list);
        }
        return list;
    }

    public void print(int lineno, PrintWriter writer) {
        final List<String> list = messages.get(lineno);

        if (list == null) {
            return;
        }

        if (writer == null) {
            writer = new PrintWriter(System.out);
        }

        for (final String messsage : list) {
            writer.print(messsage);
        }
        writer.flush();
    }
}
