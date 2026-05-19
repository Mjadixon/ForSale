package forsale;

import java.util.Collections;
import java.util.List;

/** Information available when picking a property to sell. */
public class SellContext {
    private final Player player;
    private final List<CheckCard> tableChecks;
    private final int roundNumber;

    public SellContext(Player player, List<CheckCard> tableChecks, int roundNumber) {
        this.player = player;
        this.tableChecks = List.copyOf(tableChecks);
        this.roundNumber = roundNumber;
    }

    public Player getPlayer() {
        return player;
    }

    public List<CheckCard> getTableChecks() {
        return Collections.unmodifiableList(tableChecks);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public int highestCheckGrands() {
        return tableChecks.get(0).getValueGrands();
    }

    public int lowestCheckGrands() {
        return tableChecks.get(tableChecks.size() - 1).getValueGrands();
    }
}
