package forsale;

/**
 * Fixed structure: 20 property cards, 4 players.
 * Rounds = total cards / players = 20 / 4 = 5 rounds.
 * Each round: CARDS_PER_ROUND (4) cards dealt, one per player.
 */
public final class GameRules {
    public static final int PLAYERS = 4;
    public static final int TOTAL_PROPERTY_CARDS = 20;
    public static final int CARDS_PER_ROUND = PLAYERS;  // One card per player
    public static final int BIDDING_ROUNDS = TOTAL_PROPERTY_CARDS / PLAYERS;  // 5
    public static final int SELLING_ROUNDS = BIDDING_ROUNDS;  // 5

    private GameRules() {}
}
