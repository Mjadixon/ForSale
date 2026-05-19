package forsale;

import java.util.Collections;
import java.util.List;

/** Snapshot of a bidding turn passed to human or AI controllers. */
public class BidContext {
    private final Player player;
    private final List<PropertyCard> tableCards;
    private final int highestBidThousands;
    private final int minimumBidThousands;
    private final int activeBidderCount;
    private final boolean soleBidder;

    public BidContext(
            Player player,
            List<PropertyCard> tableCards,
            int highestBidThousands,
            int minimumBidThousands,
            int activeBidderCount,
            boolean soleBidder) {
        this.player = player;
        this.tableCards = List.copyOf(tableCards);
        this.highestBidThousands = highestBidThousands;
        this.minimumBidThousands = minimumBidThousands;
        this.activeBidderCount = activeBidderCount;
        this.soleBidder = soleBidder;
    }

    public Player getPlayer() {
        return player;
    }

    public List<PropertyCard> getTableCards() {
        return Collections.unmodifiableList(tableCards);
    }

    public int getHighestBidThousands() {
        return highestBidThousands;
    }

    public int getMinimumBidThousands() {
        return minimumBidThousands;
    }

    public int getActiveBidderCount() {
        return activeBidderCount;
    }

    /** True when this player is the only one left — passing is not allowed. */
    public boolean isSoleBidder() {
        return soleBidder;
    }

    public int lowestTableValue() {
        return tableCards.get(0).getValue();
    }

    public int highestTableValue() {
        return tableCards.get(tableCards.size() - 1).getValue();
    }
}
