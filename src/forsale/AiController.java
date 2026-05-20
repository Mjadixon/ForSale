package forsale;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Intelligent computer player strategy for bidding and selling phases.
 * 
 * <b>Bidding Strategy:</b>
 * The AI uses a sophisticated evaluation system based on:
 * - Property values at the table (highest card determines maximum fair bid)
 * - Current committed bids vs. cash reserves (manages spending across rounds)
 * - Number of active bidders (higher pressure = more selective)
 * - Probabilistic decision-making (introduces variety to make AI less predictable)
 * 
 * Key behaviors:
 * - Sole bidder: Must bid minimum required amount
 * - Reserved pass: Passes when already committed beyond fair bid value
 * - Cash preservation: Maintains minimum reserves for subsequent rounds
 * - Adaptive bidding: Adjusts strategy based on player count and property values
 * 
 * <b>Selling Strategy:</b>
 * Chooses between highest and lowest-valued property based on:
 * - Batch number (early batches favor high-value properties for top checks)
 * - Check values available (adjusts based on reward tier)
 * 
 * @see PlayerController
 * @see SellController
 */
public class AiController implements PlayerController, SellController {
    // === Bidding Thresholds ===
    private static final int CASH_HIGH_THRESHOLD = 12;           // Cash above this triggers max reserve
    private static final int CASH_MID_THRESHOLD = 8;             // Cash above this triggers mid reserve
    private static final int MAX_RESERVE_THOUSANDS = 4;          // Preserve this much when cash is high
    private static final int MID_RESERVE_THOUSANDS = 3;          // Preserve this much when cash is mid
    private static final int MIN_RESERVE_THOUSANDS = 2;          // Minimum always preserved
    
    // === Property Value Thresholds ===
    private static final int LOW_PROPERTY_VALUE = 6;             // Below this, pass unless special case
    private static final int MID_PROPERTY_VALUE = 7;             // Dividing line for property evaluation
    private static final int HIGH_PROPERTY_THRESHOLD = 10;       // Only bid on cards >= this value
    private static final int VERY_HIGH_PROPERTY = 11;            // Triggers bidding in mid-value situations
    private static final int EXTREMELY_HIGH_PROPERTY = 13;       // Bypasses low-value check constraints
    private static final int TOP_PROPERTY_VALUE = 16;            // Very high card changes strategy
    
    // === Bidding Context Parameters ===
    private static final int TWO_BIDDER_THRESHOLD = 2;           // Special strategy when only 2 active bidders
    private static final int TWO_BIDDER_BID_MARGIN = 2;          // Max willing to bid above minimum
    
    // === Selling Thresholds ===
    private static final int EARLY_BATCH_THRESHOLD = 2;          // Batches <= this are early game
    private static final int LATE_BATCH_THRESHOLD = 4;           // Batches >= this are late game
    private static final int HIGH_CHECK_VALUE = 14;              // Play top property when checks are valuable
    private static final int MID_CHECK_VALUE = 10;               // Switch strategy around this value
    private static final int LOW_CHECK_VALUE = 5;                // Play low property when checks are cheap
    
    // === Probability Thresholds (decision weights) ===
    private static final double HEADS_UP_BID_PROBABILITY = 0.45;    // Odds of bidding in 2-player heads-up
    private static final double LOW_CARD_OPEN_PROBABILITY = 0.25;   // Odds of opening bid on low card
    private static final double MID_CARD_BID_PROBABILITY = 0.6;     // Odds of bidding in moderate situations
    private static final double GENERAL_PASS_PROBABILITY = 0.4;     // Odds of passing in standard case
    private static final double AGGRESSIVE_BID_PROBABILITY = 0.7;   // Odds of bidding within maxWilling range
    
    // === Bid Calculation Constants ===
    private static final int BID_BASE_MULTIPLIER = 3;            // Numerator for fair bid calculation
    private static final int BID_BASE_DIVISOR = 4;               // Denominator for fair bid calculation
    private static final int TABLE_BONUS_DIVISOR = 3;            // Divisor for table average bonus
    private static final int BID_INCREMENT_RANGE = 2;            // Random increment in bid amount

    private final Random random;

    public AiController() {
        this(new Random());
    }

    public AiController(Random random) {
        this.random = random;
    }

    @Override
    public int decideBid(BidContext context) {
        // If sole bidder, must bid minimum required amount
        if (context.isSoleBidder()) {
            return mustBid(context);
        }

        Player player = context.getPlayer();
        int minBid = context.getMinimumBidThousands();
        int committed = player.getCommittedBid();
        int spendable = committed + player.getCash();
        int cash = player.getCash();

        // Cannot afford minimum bid - must pass
        if (spendable < minBid) {
            return -1;
        }

        // Analyze the table
        int lowest = context.lowestTableValue();
        int highest = context.highestTableValue();
        int tableAvg = averageTableValue(context);
        int bidders = context.getActiveBidderCount();

        int maxForTopCard = fairBidForProperty(highest, tableAvg, bidders);
        int passThreshold = fairBidForProperty(lowest, tableAvg, bidders) + 1;

        // Already committed too much - pass to cut losses
        if (committed > 0 && committed >= passThreshold) {
            return -1;
        }
        // Lowest card isn't worth low committed bids - pass
        if (committed > 0 && lowest <= LOW_PROPERTY_VALUE && committed >= lowest) {
            return -1;
        }

        // Calculate cash reserve needed for remaining rounds
        int reserve = cash > CASH_HIGH_THRESHOLD ? MAX_RESERVE_THOUSANDS 
                    : (cash > CASH_MID_THRESHOLD ? MID_RESERVE_THOUSANDS : MIN_RESERVE_THOUSANDS);
        
        // Don't overspend on weak cards
        if (cash - (minBid - committed) < reserve && highest < EXTREMELY_HIGH_PROPERTY) {
            return -1;
        }

        // === Context-specific strategies ===
        
        // TWO-PLAYER HEADS-UP: Very selective bidding
        if (bidders == TWO_BIDDER_THRESHOLD) {
            if (highest < HIGH_PROPERTY_THRESHOLD) {
                return -1;
            }
            if (minBid > maxForTopCard) {
                return -1;
            }
            if (minBid <= maxForTopCard - TWO_BIDDER_BID_MARGIN) {
                return minBid;  // Good deal - bid now
            }
            return random.nextDouble() < HEADS_UP_BID_PROBABILITY ? -1 : minBid;
        }

        // VERY HIGH PROPERTY: Strategic bidding despite high cost
        if (highest >= TOP_PROPERTY_VALUE) {
            if (minBid > maxForTopCard) {
                return -1;
            }
            return chooseBidAmount(player, minBid, maxForTopCard, spendable);
        }

        // LOW PROPERTY: Usually pass (but open bid occasionally on good tables)
        if (highest <= MID_PROPERTY_VALUE) {
            if (minBid == 1 && committed == 0 && tableAvg <= LOW_PROPERTY_VALUE 
                    && random.nextDouble() < LOW_CARD_OPEN_PROBABILITY) {
                return 1;  // Willing to start the bidding
            }
            return -1;
        }

        // STANDARD CASE: Mid-range properties
        if (minBid > maxForTopCard) {
            return -1;
        }
        if (minBid <= passThreshold && highest >= VERY_HIGH_PROPERTY 
                && random.nextDouble() < MID_CARD_BID_PROBABILITY) {
            return chooseBidAmount(player, minBid, maxForTopCard, spendable);
        }
        if (random.nextDouble() < GENERAL_PASS_PROBABILITY) {
            return -1;
        }
        return chooseBidAmount(player, minBid, maxForTopCard, spendable);
    }

    @Override
    public PropertyCard chooseProperty(SellContext context) {
        List<PropertyCard> hand = context.getPlayer().getPropertiesHighToLow();
        PropertyCard highest = hand.get(0);
        PropertyCard lowest = hand.get(hand.size() - 1);
        int topCheck = context.highestCheckGrands();
        int batch = context.getBatchNumber();

        // Early game or valuable checks: prioritize high properties for top checks
        if (batch <= EARLY_BATCH_THRESHOLD || topCheck >= HIGH_CHECK_VALUE) {
            return highest;
        }
        
        // Late game or cheap checks: play low properties
        if (batch >= LATE_BATCH_THRESHOLD || topCheck <= LOW_CHECK_VALUE) {
            return lowest;
        }
        
        // Mid-range checks: mid-game strategy
        if (topCheck >= MID_CHECK_VALUE) {
            return hand.get(Math.min(1, hand.size() - 1));  // Second-best
        }
        
        // Default: play median property
        return hand.get(hand.size() / 2);
    }

    /**
     * Calculate a fair bid for a property based on its rank and market conditions.
     * Formula: (rank * 3/4) + tableAvg adjustment + bidder pressure adjustment
     * 
     * @param propertyRank The value of the card at the table (1-20)
     * @param tableAvg Average value of all cards in this round
     * @param bidders Number of active bidders (more competition = higher fair bid)
     * @return Maximum amount this AI considers fair for this property (in thousands)
     */
    private int fairBidForProperty(int propertyRank, int tableAvg, int bidders) {
        int base = (propertyRank * BID_BASE_MULTIPLIER) / BID_BASE_DIVISOR;
        int tableBonus = Math.max(0, (tableAvg - propertyRank) / TABLE_BONUS_DIVISOR);
        int pressure = Math.max(0, bidders - TWO_BIDDER_THRESHOLD);
        return Math.max(1, base + tableBonus + pressure);
    }

    /**
     * Calculate average value of cards currently on the table.
     * Used to assess overall property quality and adjust bidding strategy.
     * 
     * @param context The current bidding round context
     * @return Average property value rounded down (in range 1-20)
     */
    private int averageTableValue(BidContext context) {
        List<PropertyCard> cards = context.getTableCards();
        int sum = 0;
        for (PropertyCard card : cards) {
            sum += card.getValue();
        }
        return sum / cards.size();
    }

    /**
     * Determines bid amount when AI is the sole remaining bidder.
     * If already committed, return committed amount if affordable.
     * Otherwise, bid minimum if affordable, else pass.
     * 
     * @param context The current bidding round context
     * @return Bid amount or -1 to pass
     */
    private int mustBid(BidContext context) {
        Player player = context.getPlayer();
        int minBid = context.getMinimumBidThousands();
        
        // Already committed - return that amount if still affordable
        if (player.getCommittedBid() >= minBid) {
            return player.getCommittedBid();
        }
        
        // Not committed yet - must bid the minimum if affordable
        if (player.getCash() + player.getCommittedBid() >= minBid) {
            return minBid;
        }
        
        // Cannot afford minimum - must pass
        return -1;
    }

    /**
     * Choose a specific bid amount within the AI's willing range.
     * Strategy: Bid minimum when certain, or a higher amount to appear aggressive.
     * Never exceeds available cash or the maximum willing amount.
     * 
     * @param player The AI player making the decision
     * @param minBid The minimum bid required to stay in this round
     * @param maxWilling Maximum amount AI is willing to pay for this property
     * @param spendable Total cash available (committed + current cash)
     * @return Bid amount in thousands, or -1 if cannot afford minimum
     */
    private int chooseBidAmount(Player player, int minBid, int maxWilling, int spendable) {
        int cap = Math.min(spendable, maxWilling);
        if (minBid > cap) {
            return -1;
        }
        
        // Bid minimum if it's the only option or with high probability
        if (cap == minBid || random.nextDouble() < AGGRESSIVE_BID_PROBABILITY) {
            return minBid;
        }
        
        // Be more aggressive: increase bid slightly to signal strength
        int target = Math.min(cap, minBid + 1 + random.nextInt(BID_INCREMENT_RANGE));
        
        // Check if can actually afford target amount
        if (target - player.getCommittedBid() > player.getCash()) {
            return minBid;  // Fall back to minimum if can't afford aggressive bid
        }
        return target;
    }
}
