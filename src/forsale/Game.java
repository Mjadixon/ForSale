package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Two phases: bidding then selling until all properties are gone. Highest balance wins.
 */
public class Game {
    public static final int STARTING_CASH_THOUSANDS = 18;

    private final ConsoleUI ui;
    private final List<GameParticipant> participants;
    private final PropertyDeck deck;

    public Game(ConsoleUI ui, List<GameParticipant> participants, PropertyDeck deck) {
        this.ui = ui;
        this.participants = new ArrayList<>(participants);
        this.deck = deck;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();
        for (GameParticipant participant : participants) {
            players.add(participant.getPlayer());
        }
        return players;
    }

    public static Game setupFromConsole(ConsoleUI ui) {
        ui.clearPanel();
        ui.println("=== FOR SALE ===");
        ui.println("4 players | Phase 1: bid | Phase 2: sell all cards");
        ui.println("Highest balance at the end wins.");
        ui.logMove("New game");

        List<GameParticipant> roster = new ArrayList<>();
        for (int i = 1; i <= GameRules.PLAYERS; i++) {
            ui.clearPanel();
            ui.println("Player " + i + " of " + GameRules.PLAYERS);
            boolean human = ui.readYesNo("Human player?");

            String name;
            if (human) {
                name = ui.readNonEmptyLine("Enter name:");
            } else {
                name = "Computer " + i;
            }

            Player player = new Player(name, STARTING_CASH_THOUSANDS);
            HumanController humanController = new HumanController(ui);
            AiController aiController = new AiController();
            if (human) {
                roster.add(new GameParticipant(player, humanController, humanController, true));
            } else {
                roster.add(new GameParticipant(player, aiController, aiController, false));
            }
            ui.logMove(name + " joined");
        }

        return new Game(ui, roster, new PropertyDeck());
    }

    public void play() {
        GameHelp.showOverview(ui);

        ui.clearPanel();
        ui.println("Roster:");
        for (GameParticipant participant : participants) {
            ui.println("  " + participant.getLabel());
        }
        ui.pressEnterToContinue();

        BiddingPhase bidding = new BiddingPhase(ui, participants, deck);
        bidding.play();

        SellingPhase selling = new SellingPhase(ui, participants, new CheckDeck());
        selling.play();

        announceWinner();
    }

    /** Winner: highest balance (checks won in Phase 2 + coins left). */
    public void announceWinner() {
        List<Player> ranked = new ArrayList<>(getPlayers());
        ranked.sort(Comparator
                .comparingInt(Player::getTotalWealthThousands)
                .thenComparingInt(Player::getCheckTotalThousands)
                .reversed());

        ui.logMove("Final scoring");
        ui.clearPanel();
        ui.println("=== WINNER ===");
        ui.println("Highest balance wins (checks + coins).");

        ui.println();
        for (Player player : ranked) {
            ui.showWealthBreakdown(player);
        }

        int topScore = ranked.get(0).getTotalWealthThousands();
        List<Player> winners = new ArrayList<>();
        for (Player player : ranked) {
            if (player.getTotalWealthThousands() == topScore) {
                winners.add(player);
            }
        }

        ui.println();
        if (winners.size() == 1) {
            Player w = winners.get(0);
            String msg = "Winner: " + w.getName()
                    + " with balance " + Player.formatMoney(topScore)
                    + " (" + Player.formatMoney(w.getCheckTotalThousands()) + " checks, "
                    + Player.formatMoney(w.getCash()) + " coins)";
            ui.println(msg);
            ui.logMove(msg);
        } else {
            ui.println("Tie for highest balance:");
            for (Player player : winners) {
                ui.println("  " + player.getName() + " — " + Player.formatMoney(player.getTotalWealthThousands()));
            }
        }
    }
}
