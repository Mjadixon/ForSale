package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Two phases only: five bidding rounds (4 cards each), then five selling rounds.
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
        ui.println("4 players, 2 phases, 5 rounds each.");
        ui.println("Phase 1: bid on 4 cards per round.");
        ui.println("Phase 2: give a card, win checks.");
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

    /**
     * Winner: highest total of Phase 2 checks plus leftover coins from Phase 1.
     */
    public void announceWinner() {
        List<Player> ranked = new ArrayList<>(getPlayers());
        ranked.sort(Comparator
                .comparingInt(Player::getTotalWealthThousands)
                .thenComparingInt(Player::getCheckTotalThousands)
                .reversed());

        ui.logMove("Final scoring");
        ui.clearPanel();
        ui.println("=== WINNER ===");
        ui.println("Each check you won in Phase 2 counts toward your total.");
        ui.println("Winner = most checks + coins combined.");

        Player bestChecks = ranked.stream()
                .max(Comparator.comparingInt(Player::getCheckTotalThousands))
                .orElse(ranked.get(0));
        ui.println();
        ui.println("Most from checks: " + bestChecks.getName()
                + " (" + Player.formatMoney(bestChecks.getCheckTotalThousands()) + ")");

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
                    + " with " + Player.formatMoney(w.getCheckTotalThousands()) + " in checks"
                    + " and " + Player.formatMoney(w.getCash()) + " in coins"
                    + " (" + Player.formatMoney(topScore) + " total)";
            ui.println(msg);
            ui.logMove(msg);
        } else {
            ui.println("Tie for the win:");
            for (Player player : winners) {
                ui.println("  " + player.getName());
            }
        }
    }
}
