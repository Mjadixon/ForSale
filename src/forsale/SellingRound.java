package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One selling batch: checks on the table, each player picks one property won in Phase 1,
 * reveal by rank, award checks to balance. Then Phase 2 advances to the next batch.
 */
public class SellingRound {
    private final GameDisplay ui;

    public SellingRound(GameDisplay ui) {
        this.ui = ui;
    }

    public void playBatch(
            List<GameParticipant> participants,
            CheckDeck checkDeck,
            int batchNumber,
            int totalBatches) {
        int playersWithCards = countPlayersWithProperties(participants);
        if (playersWithCards == 0) {
            return;
        }

        int dealCount = Math.min(GameRules.CARDS_PER_ROUND, Math.min(playersWithCards, checkDeck.remaining()));
        if (dealCount == 0) {
            ui.logMove("Batch " + batchNumber + " skipped (no checks)");
            return;
        }

        List<CheckCard> tableChecks = checkDeck.dealRound(dealCount);

        ui.setRoundContext("Phase 2 | Batch " + batchNumber + "/" + totalBatches);
        ui.logMove("Checks: "
                + tableChecks.stream()
                        .map(c -> Currency.format(c.getValueGrands()))
                        .collect(Collectors.joining(", ")));

        ui.clearPanel();
        ui.println("=== Selling batch " + batchNumber + " of " + totalBatches + " ===");
        ui.println("Play one property card you won in Phase 1.");
        ui.println("Checks on table (rank 1 property wins rank 1 check):");
        for (int i = 0; i < tableChecks.size(); i++) {
            ui.println("  Rank " + (i + 1) + ": " + tableChecks.get(i));
        }
        ui.showPropertiesRemaining(participants);
        ui.showAllBalances(participants);

        boolean humanSelling = participants.stream().anyMatch(GameParticipant::isHuman);
        if (humanSelling) {
            ui.println();
            ui.println("Select a card to play. Type help for rules.");
            ui.pressEnterToContinue();
        }

        List<PropertyPlay> plays = collectPlays(participants, tableChecks, batchNumber, totalBatches);
        if (plays.isEmpty()) {
            ui.logMove("Batch " + batchNumber + ": no cards played");
            return;
        }

        awardByRank(participants, plays, tableChecks, batchNumber, totalBatches);
    }

    private List<PropertyPlay> collectPlays(
            List<GameParticipant> participants,
            List<CheckCard> tableChecks,
            int batchNumber,
            int totalBatches) {
        List<PropertyPlay> plays = new ArrayList<>();
        for (GameParticipant seller : participants) {
            Player player = seller.getPlayer();
            if (!player.hasProperties()) {
                continue;
            }

            SellContext context = new SellContext(player, tableChecks, batchNumber, totalBatches);
            PropertyCard chosen = seller.getSellController().chooseProperty(context);
            player.removeProperty(chosen);
            plays.add(new PropertyPlay(seller, chosen));
            ui.logMove(player.getName() + " plays #" + chosen.getValue());
        }
        return plays;
    }

    private void awardByRank(
            List<GameParticipant> participants,
            List<PropertyPlay> plays,
            List<CheckCard> tableChecks,
            int batchNumber,
            int totalBatches) {
        plays.sort(Comparator.comparingInt((PropertyPlay p) -> p.getProperty().getValue()).reversed());

        ui.clearPanel();
        ui.println("--- Batch " + batchNumber + " reveal (high to low) ---");

        int awards = Math.min(plays.size(), tableChecks.size());
        for (int rank = 0; rank < awards; rank++) {
            PropertyPlay play = plays.get(rank);
            CheckCard check = tableChecks.get(rank);
            Player player = play.getPlayer();
            int before = player.getTotalWealthThousands();
            int after = player.addCheck(check.getValueGrands());

            ui.println("Rank " + (rank + 1) + ": " + player.getName()
                    + "  " + play.getProperty()
                    + "  ->  " + check
                    + "  (+ " + Currency.format(check.getValueGrands()) + ")"
                    + "  balance " + Currency.format(before)
                    + " -> " + Currency.format(after));
            ui.logMove(player.getName() + " +" + Currency.format(check.getValueGrands())
                    + " bal " + Currency.format(after));
        }

        for (int rank = awards; rank < plays.size(); rank++) {
            PropertyPlay play = plays.get(rank);
            ui.println("Rank " + (rank + 1) + ": " + play.getPlayer().getName()
                    + "  " + play.getProperty() + "  (no check)");
        }

        ui.println();
        ui.showAllBalances(participants);

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
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
