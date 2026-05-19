package forsale;

import java.util.Collections;
import java.util.List;

/** Information when picking a property to compete for one check on the table. */
public class SellContext {
    private final Player player;
    private final List<CheckCard> allChecksThisBatch;
    private final CheckCard currentCheck;
    private final int checkIndex;
    private final int batchNumber;

    public SellContext(
            Player player,
            List<CheckCard> allChecksThisBatch,
            CheckCard currentCheck,
            int checkIndex,
            int batchNumber) {
        this.player = player;
        this.allChecksThisBatch = List.copyOf(allChecksThisBatch);
        this.currentCheck = currentCheck;
        this.checkIndex = checkIndex;
        this.batchNumber = batchNumber;
    }

    public Player getPlayer() {
        return player;
    }

    public List<CheckCard> getAllChecksThisBatch() {
        return Collections.unmodifiableList(allChecksThisBatch);
    }

    public CheckCard getCurrentCheck() {
        return currentCheck;
    }

    public int getCurrentCheckGrands() {
        return currentCheck.getValueGrands();
    }

    public int getCheckIndex() {
        return checkIndex;
    }

    public int getBatchNumber() {
        return batchNumber;
    }

    public boolean isFirstCheckInBatch() {
        return checkIndex == 0;
    }

    public boolean isLastCheckInBatch() {
        return checkIndex == allChecksThisBatch.size() - 1;
    }
}
