package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One selling batch: four checks on the table. Each player selects one property.
 * All reveal; highest property rank wins the top check, and so on. Check values
 * are added to each winner's balance.
 */
public class SellingRound {
    private final ConsoleUI ui;

    public SellingRound(ConsoleUI ui) {
        this.ui = ui;
    }

    public void playBatch(List<GameParticipant> participants, CheckDeck checkDeck, int batchNumber) {
        if (checkDeck.remaining() < GameRules.CARDS_PER_ROUND) {
            ui.logMove("Batch " + batchNumber + " skipped (not enough checks)");
            return;
        }

        List<CheckCard> tableChecks = checkDeck.dealRound(GameRules.CARDS_PER_ROUND);

        ui.logMove("Batch " + batchNumber + "/5 checks: "
                + tableChecks.stream()
                        .map(c -> Currency.format(c.getValueGrands()))
                        .collect(Collectors.joining(", ")));

        ui.clearPanel();
        ui.println("=== Selling batch " + batchNumber + " of " + GameRules.SELLING_ROUNDS + " ===");
        ui.println("Checks on table (rank 1 gets check 1, etc.):");
        for (int i = 0; i < tableChecks.size(); i++) {
            ui.println("  Rank " + (i + 1) + ": " + tableChecks.get(i));
        }
        ui.showAllBalances(participants);

        boolean humanSelling = participants.stream().anyMatch(GameParticipant::isHuman);
        if (humanSelling) {
            ui.println();
            ui.println("Pick one property to play. Highest # beats lower #.");
            ui.println("Type help for rules, then continue.");
            ui.pressEnterToContinue();
        }

        List<PropertyPlay> plays = collectPlays(participants, tableChecks, batchNumber);
        if (plays.isEmpty()) {
            ui.logMove("Batch " + batchNumber + ": no cards played");
            return;
        }

        awardByRank(participants, plays, tableChecks, batchNumber);
    }

    private List<PropertyPlay> collectPlays(
            List<GameParticipant> participants, List<CheckCard> tableChecks, int batchNumber) {
        List<PropertyPlay> plays = new ArrayList<>();
        for (GameParticipant seller : participants) {
            Player player = seller.getPlayer();
            if (!player.hasProperties()) {
                ui.logMove(player.getName() + " has no property for batch " + batchNumber);
                continue;
            }

            SellContext context = new SellContext(player, tableChecks, batchNumber);
            PropertyCard chosen = seller.getSellController().chooseProperty(context);
            player.removeProperty(chosen);
            plays.add(new PropertyPlay(seller, chosen));

            ui.logMove(player.getName() + " selected #" + chosen.getValue());
        }
        return plays;
    }

    private void awardByRank(
            List<GameParticipant> participants,
            List<PropertyPlay> plays,
            List<CheckCard> tableChecks,
            int batchNumber) {
        plays.sort(Comparator.comparingInt((PropertyPlay p) -> p.getProperty().getValue()).reversed());

        ui.clearPanel();
        ui.println("--- Reveal (ranked high to low) ---");

        int awards = Math.min(plays.size(), tableChecks.size());
        for (int rank = 0; rank < awards; rank++) {
            PropertyPlay play = plays.get(rank);
            CheckCard check = tableChecks.get(rank);
            Player player = play.getPlayer();

            player.addCheck(check.getValueGrands());
            int after = player.getTotalWealthThousands();

            String line = "Rank " + (rank + 1) + ": " + player.getName()
                    + "  property " + play.getProperty()
                    + "  ->  " + check
                    + "  (+ " + Currency.format(check.getValueGrands()) + ")"
                    + "  balance " + Currency.format(after);
            ui.println(line);
            ui.logMove(player.getName() + " rank " + (rank + 1) + " +"
                    + Currency.format(check.getValueGrands()) + " bal " + Currency.format(after));
        }

        for (int rank = awards; rank < plays.size(); rank++) {
            PropertyPlay play = plays.get(rank);
            ui.println("Rank " + (rank + 1) + ": " + play.getPlayer().getName()
                    + "  " + play.getProperty() + "  (no check left)");
        }

        ui.println();
        ui.showAllBalances(participants);

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }
}
