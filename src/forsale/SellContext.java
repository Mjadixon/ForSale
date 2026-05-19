package forsale;

import java.util.Collections;
import java.util.List;

/** Information when picking a property for one selling batch (4 checks on table). */
public class SellContext {
    private final Player player;
    private final List<CheckCard> tableChecks;
    private final int batchNumber;

    public SellContext(Player player, List<CheckCard> tableChecks, int batchNumber) {
        this.player = player;
        this.tableChecks = List.copyOf(tableChecks);
        this.batchNumber = batchNumber;
    }

    public Player getPlayer() {
        return player;
    }

    public List<CheckCard> getTableChecks() {
        return Collections.unmodifiableList(tableChecks);
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public int highestCheckGrands() {
        return tableChecks.get(0).getValueGrands();
    }

    public int lowestCheckGrands() {
        return tableChecks.get(tableChecks.size() - 1).getValueGrands();
    }
}
