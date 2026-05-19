package forsale;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * AI for bidding and selling with table-aware heuristics.
 */
public class AiController implements PlayerController, SellController {
    private final Random random;

    public AiController() {
        this(new Random());
    }

    public AiController(Random random) {
        this.random = random;
    }

    @Override
    public int decideBid(BidContext context) {
        if (context.isSoleBidder()) {
            return mustBid(context);
        }

        Player player = context.getPlayer();
        int minBid = context.getMinimumBidThousands();
        int committed = player.getCommittedBid();
        int spendable = committed + player.getCash();
        int cash = player.getCash();

        if (spendable < minBid) {
            return -1;
        }

        int lowest = context.lowestTableValue();
        int highest = context.highestTableValue();
        int tableAvg = averageTableValue(context);
        int bidders = context.getActiveBidderCount();

        int maxForTopCard = fairBidForProperty(highest, tableAvg, bidders);
        int passThreshold = fairBidForProperty(lowest, tableAvg, bidders) + 1;

        // Passing now likely wins the current lowest card — stop if bid is too high for that prize.
        if (committed > 0 && committed >= passThreshold) {
            return -1;
        }
        if (committed > 0 && lowest <= 6 && committed >= lowest) {
            return -1;
        }

        // Keep reserves for later rounds in the first half of the game.
        int reserve = cash > 12 ? 4 : (cash > 8 ? 3 : 2);
        if (cash - (minBid - committed) < reserve && highest < 13) {
            return -1;
        }

        // Head-to-head for the top card.
        if (bidders == 2) {
            if (highest < 10) {
                return -1;
            }
            if (minBid > maxForTopCard) {
                return -1;
            }
            if (minBid <= maxForTopCard - 2) {
                return minBid;
            }
            return random.nextDouble() < 0.45 ? -1 : minBid;
        }

        // Strong table — compete for the top card.
        if (highest >= 16) {
            if (minBid > maxForTopCard) {
                return -1;
            }
            return chooseBidAmount(player, minBid, maxForTopCard, spendable);
        }

        // Weak table — pass unless opening bid is cheap.
        if (highest <= 7) {
            if (minBid == 1 && committed == 0 && tableAvg <= 6 && random.nextDouble() < 0.25) {
                return 1;
            }
            return -1;
        }

        // Medium table — bid only if price is fair.
        if (minBid > maxForTopCard) {
            return -1;
        }
        if (minBid <= passThreshold && highest >= 11 && random.nextDouble() < 0.6) {
            return chooseBidAmount(player, minBid, maxForTopCard, spendable);
        }
        if (random.nextDouble() < 0.4) {
            return -1;
        }
        return chooseBidAmount(player, minBid, maxForTopCard, spendable);
    }

    /**
     * Rough max bid (in thousands) worth paying for a property of this rank on this table.
     */
    private int fairBidForProperty(int propertyRank, int tableAvg, int bidders) {
        int base = (propertyRank * 3) / 4;
        int tableBonus = Math.max(0, (tableAvg - propertyRank) / 3);
        int pressure = Math.max(0, bidders - 2);
        return Math.max(1, base + tableBonus + pressure);
    }

    private int averageTableValue(BidContext context) {
        List<PropertyCard> cards = context.getTableCards();
        int sum = 0;
        for (PropertyCard card : cards) {
            sum += card.getValue();
        }
        return sum / cards.size();
    }

    private int mustBid(BidContext context) {
        Player player = context.getPlayer();
        int minBid = context.getMinimumBidThousands();
        if (player.getCommittedBid() >= minBid) {
            return player.getCommittedBid();
        }
        if (player.getCash() + player.getCommittedBid() >= minBid) {
            return minBid;
        }
        return -1;
    }

    private int chooseBidAmount(Player player, int minBid, int maxWilling, int spendable) {
        int cap = Math.min(spendable, maxWilling);
        if (minBid > cap) {
            return -1;
        }
        if (cap == minBid || random.nextDouble() < 0.7) {
            return minBid;
        }
        int target = Math.min(cap, minBid + 1 + random.nextInt(2));
        if (target - player.getCommittedBid() > player.getCash()) {
            return minBid;
        }
        return target;
    }

    @Override
    public PropertyCard chooseProperty(SellContext context) {
        List<PropertyCard> properties = context.getPlayer().getProperties();
        int topCheck = context.highestCheckGrands();
        int bottomCheck = context.lowestCheckGrands();

        PropertyCard best = properties.stream()
                .max(Comparator.comparingInt(PropertyCard::getValue))
                .orElseThrow();
        PropertyCard worst = properties.stream()
                .min(Comparator.comparingInt(PropertyCard::getValue))
                .orElseThrow();

        if (topCheck >= 15) {
            return best;
        }
        if (bottomCheck <= 4) {
            return worst;
        }
        if (topCheck >= 10 && random.nextDouble() < 0.75) {
            return best;
        }
        if (random.nextDouble() < 0.4) {
            return worst;
        }
        return properties.get(random.nextInt(properties.size()));
    }
}
