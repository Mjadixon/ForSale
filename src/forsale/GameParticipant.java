package forsale;

/**
 * Links a {@link Player} to human or AI controllers for bidding and selling.
 */
public class GameParticipant {
    private final Player player;
    private final PlayerController bidController;
    private final SellController sellController;
    private final boolean human;

    public GameParticipant(
            Player player,
            PlayerController bidController,
            SellController sellController,
            boolean human) {
        this.player = player;
        this.bidController = bidController;
        this.sellController = sellController;
        this.human = human;
    }

    public Player getPlayer() {
        return player;
    }

    public PlayerController getBidController() {
        return bidController;
    }

    public PlayerController getController() {
        return bidController;
    }

    public SellController getSellController() {
        return sellController;
    }

    public boolean isHuman() {
        return human;
    }

    public String getLabel() {
        return human ? player.getName() : player.getName() + " (AI)";
    }
}
