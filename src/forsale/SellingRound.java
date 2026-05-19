package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Phase 2: checks are on the table from highest to lowest. For each check, every
 * player selects one property they won in Phase 1. The highest property number
 * wins that check (added to balance). Repeat until all properties are gone.
 */
public class SellingRound {
    private final ConsoleUI ui;

    public SellingRound(ConsoleUI ui) {
        this.ui = ui;
    }

    /**
     * @return true if a batch was played
     */
    public boolean playBatch(List<GameParticipant> participants, CheckDeck checkDeck, int batchNumber) {
        int sellers = countPlayersWithProperties(participants);
        if (sellers == 0) {
            return false;
        }

        int dealCount = Math.min(GameRules.CARDS_PER_ROUND, Math.min(sellers, checkDeck.remaining()));
        if (dealCount == 0) {
            return false;
        }

        List<CheckCard> tableChecks = checkDeck.dealRound(dealCount);
        ui.logMove("Batch " + batchNumber + " checks: "
                + tableChecks.stream()
                        .map(c -> Currency.format(c.getValueGrands()))
                        .collect(Collectors.joining(", ")));

        ui.clearPanel();
        ui.println("=== Selling batch " + batchNumber + " ===");
        ui.println("Checks on table (awarded high to low):");
        for (int i = 0; i < tableChecks.size(); i++) {
            ui.println("  " + (i + 1) + ". " + tableChecks.get(i));
        }
        ui.showAllCheckTotals(participants);

        boolean humanSelling = participants.stream().anyMatch(GameParticipant::isHuman);
        if (humanSelling) {
            ui.println();
            ui.println("For each check: pick a property (high # beats low #).");
            ui.pressEnterToContinue();
        }

        for (int checkIndex = 0; checkIndex < tableChecks.size(); checkIndex++) {
            CheckCard check = tableChecks.get(checkIndex);
            if (countPlayersWithProperties(participants) == 0) {
                break;
            }
            awardCheckForHighestProperty(participants, tableChecks, check, checkIndex, batchNumber);
        }

        return true;
    }

    private void awardCheckForHighestProperty(
            List<GameParticipant> participants,
            List<CheckCard> tableChecks,
            CheckCard check,
            int checkIndex,
            int batchNumber) {
        ui.clearPanel();
        ui.println("Check " + (checkIndex + 1) + " of " + tableChecks.size()
                + " on table: " + check);
        ui.println("Select a property. Highest number wins this check.");

        List<PropertyPlay> plays = collectPlays(participants, tableChecks, check, checkIndex, batchNumber);
        if (plays.isEmpty()) {
            ui.logMove("No cards played for " + Currency.format(check.getValueGrands()));
            return;
        }

        plays.sort(Comparator.comparingInt((PropertyPlay p) -> p.getProperty().getValue()).reversed());

        PropertyPlay winner = plays.get(0);
        winner.getPlayer().addCheck(check.getValueGrands());

        ui.clearPanel();
        ui.println("--- Reveal (high to low) ---");
        for (int i = 0; i < plays.size(); i++) {
            PropertyPlay play = plays.get(i);
            String tag = i == 0 ? " <- WINS " + check : "";
            ui.println((i + 1) + ". " + play.getPlayer().getName()
                    + " played " + play.getProperty() + tag);
        }

        ui.logMove("WIN " + winner.getPlayer().getName() + " #"
                + winner.getProperty().getValue() + " gets " + Currency.format(check.getValueGrands()));

        ui.println();
        ui.println(winner.getPlayer().getName() + " wins " + check
                + " (balance: " + Currency.format(winner.getPlayer().getTotalWealthThousands()) + ")");
        ui.showAllCheckTotals(participants);

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }

    private List<PropertyPlay> collectPlays(
            List<GameParticipant> participants,
            List<CheckCard> tableChecks,
            CheckCard check,
            int checkIndex,
            int batchNumber) {
        List<PropertyPlay> plays = new ArrayList<>();
        for (GameParticipant seller : participants) {
            Player player = seller.getPlayer();
            if (!player.hasProperties()) {
                continue;
            }

            SellContext context = new SellContext(player, tableChecks, check, checkIndex, batchNumber);
            PropertyCard chosen = seller.getSellController().chooseProperty(context);
            player.removeProperty(chosen);
            plays.add(new PropertyPlay(seller, chosen));

            ui.logMove(player.getName() + " plays #" + chosen.getValue()
                    + " for " + Currency.format(check.getValueGrands()));
        }
        return plays;
    }

    private static int countPlayersWithProperties(List<GameParticipant> participants) {
        int count = 0;
        for (GameParticipant participant : participants) {
            if (participant.getPlayer().hasProperties()) {
                count++;
            }
        }
        return count;
    }
}
