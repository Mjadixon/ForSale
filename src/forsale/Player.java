package forsale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A player with cash (in thousands), owned properties, and a bid committed this round.
 */
public class Player {
    private final String name;
    private int cash;
    private int committedBid;
    private final List<PropertyCard> properties = new ArrayList<>();
    /** Phase 2 check cards, stored in thousands (same unit as cash). */
    private final List<Integer> checks = new ArrayList<>();

    public Player(String name, int startingCash) {
        this.name = name;
        this.cash = startingCash;
    }

    public String getName() {
        return name;
    }

    public int getCash() {
        return cash;
    }

    public int getCommittedBid() {
        return committedBid;
    }

    public List<PropertyCard> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    /** Properties still held, sorted highest to lowest (best first). */
    public List<PropertyCard> getPropertiesHighToLow() {
        List<PropertyCard> sorted = new ArrayList<>(properties);
        sorted.sort(Comparator.reverseOrder());
        return Collections.unmodifiableList(sorted);
    }

    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public List<Integer> getChecks() {
        return Collections.unmodifiableList(checks);
    }

    public void addProperty(PropertyCard card) {
        properties.add(card);
    }

    public boolean removeProperty(PropertyCard card) {
        return properties.remove(card);
    }

    /**
     * Adds a check's value to this player's balance (tracked in checks + total wealth).
     *
     * @return new total balance after adding this check
     */
    public int addCheck(int valueThousands) {
        if (valueThousands < 0) {
            throw new IllegalArgumentException("Check value cannot be negative.");
        }
        checks.add(valueThousands);
        return getTotalWealthThousands();
    }

    public int getCheckTotalThousands() {
        int total = 0;
        for (int check : checks) {
            total += check;
        }
        return total;
    }

    /** Checks plus coins still in hand (committed bids should be zero between phases). */
    public int getTotalWealthThousands() {
        return cash + getCheckTotalThousands();
    }

    /** Money still available after the bid locked in for this round. */
    public int availableCash() {
        return cash;
    }

    /**
     * Sets total bid for this round. Additional cash is removed from hand.
     */
    public void setCommittedBid(int totalBid) {
        if (totalBid < committedBid) {
            throw new IllegalArgumentException("New bid must be at least the current committed bid.");
        }
        int additional = totalBid - committedBid;
        if (additional > cash) {
            throw new IllegalArgumentException("Not enough cash for that bid.");
        }
        cash -= additional;
        committedBid = totalBid;
    }

    /**
     * Pass: return half the committed bid (rounded down), rest stays out of play.
     */
    public int refundHalfBid() {
        int refund = committedBid / 2;
        cash += refund;
        int toBank = committedBid - refund;
        committedBid = 0;
        return toBank;
    }

    /**
     * Win the auction: full committed bid is paid to the bank (already deducted).
     */
    public int payFullBidToBank() {
        int toBank = committedBid;
        committedBid = 0;
        return toBank;
    }

    public void clearRoundBid() {
        cash += committedBid;
        committedBid = 0;
    }

    @Override
    public String toString() {
        return name + " (" + formatMoney(cash) + ", bid " + formatMoney(committedBid) + ")";
    }

    /** @param amountThousands internal units (3 = $3,000) */
    public static String formatMoney(int amountThousands) {
        return Currency.format(amountThousands);
    }
}
