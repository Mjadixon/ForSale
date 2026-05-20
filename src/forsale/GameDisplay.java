package forsale;

import java.util.List;

/**
 * Interface for all game display implementations (Console, Swing, Web, etc).
 * Separates game logic from presentation.
 */
public interface GameDisplay {
    
    // === Setup & Context ===
    void setRoundContext(String label);
    void logMove(String move);
    void setRestartListener(RestartListener listener);
    
    // === Display Methods ===
    void clearPanel();
    void panelLine(String line);
    void println(String message);
    void println();
    void printDivider();
    void refresh();
    
    void showTable(List<PropertyCard> tableCards);
    void showAllBalances(List<GameParticipant> participants);
    void showPropertiesRemaining(List<GameParticipant> participants);
    void showPlayerSummary(Player player);
    void showFinalScores(List<Player> rankedPlayers);
    
    // === Input Methods ===
    String readLineAllowHelp(boolean allowHelp);
    void pressEnterToContinue();
    int readIntInRange(String prompt, int min, int max);
    String readNonEmptyLine(String prompt);
    boolean readYesNo(String prompt);
    int readBidOrPass(Player player, int minimumBidThousands, int highestBidThousands, boolean soleBidder);
    PropertyCard readPropertyChoice(Player player, SellContext context);
}
