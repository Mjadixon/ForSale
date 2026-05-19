package forsale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Running list of every move, shown in the left column. */
public class MoveLog {
    private final int maxEntries;
    private final List<String> entries = new ArrayList<>();

    public MoveLog(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void add(String move) {
        entries.add(move);
        while (entries.size() > maxEntries) {
            entries.remove(0);
        }
    }

    public List<String> snapshot() {
        return Collections.unmodifiableList(entries);
    }
}
