package forsale;

/**
 * Decides bids for a player (human console input or AI logic).
 *
 * @return total bid in thousands, or -1 to pass
 */
public interface PlayerController {
    int decideBid(BidContext context);
}
