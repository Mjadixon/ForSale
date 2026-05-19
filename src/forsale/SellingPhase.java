package forsale;

import java.util.List;

/**
 * Phase 2: one batch per property won in Phase 1. After each batch, step to the next
 * until every card has been played and all batches are done.
 */
public class SellingPhase {
    private final ConsoleUI ui;
    private final List<GameParticipant> participants;
    private final CheckDeck checkDeck;
    private final SellingRound roundRunner;

    public SellingPhase(ConsoleUI ui, List<GameParticipant> participants, CheckDeck checkDeck) {
        this.ui = ui;
        this.participants = participants;
        this.checkDeck = checkDeck;
        this.roundRunner = new SellingRound(ui);
    }

    public void play() {
        int totalBatches = countBatchesFromCardsWon(participants);

        ui.setRoundContext("Phase 2 | Selling");
        ui.logMove("Phase 2: " + totalBatches + " batches (1 per card won)");
        ui.clearPanel();
        ui.println("=== PHASE 2: SELLING ===");
        ui.println("You won properties in Phase 1.");
        ui.println("Selling batches: " + totalBatches + " (one batch per card you kept).");
        ui.println("Each batch: 4 checks, pick 1 property, reveal by rank.");
        ui.println("After each batch -> next batch until all cards are gone.");
        ui.showPropertiesRemaining(participants);

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }

        for (int batch = 1; batch <= totalBatches; batch++) {
            if (!anyPlayerHasProperties()) {
                ui.logMove("All property cards sold early (batch " + (batch - 1) + ")");
                break;
            }
            if (checkDeck.remaining() == 0) {
                ui.logMove("Check deck empty at batch " + batch);
                break;
            }

            roundRunner.playBatch(participants, checkDeck, batch, totalBatches);

            if (!anyPlayerHasProperties()) {
                ui.logMove("All cards played after batch " + batch);
                break;
            }

            if (batch < totalBatches) {
                ui.clearPanel();
                ui.println("Batch " + batch + " of " + totalBatches + " complete.");
                ui.showPropertiesRemaining(participants);
                ui.println();
                ui.println("Press Enter for the next batch...");
                if (participants.stream().anyMatch(GameParticipant::isHuman)) {
                    ui.pressEnterToContinue();
                }
            }
        }

        ui.logMove("Phase 2 complete");
        ui.clearPanel();
        ui.println("=== SELLING DONE ===");
        if (anyPlayerHasProperties()) {
            ui.println("Warning: some property cards were not sold.");
            ui.showPropertiesRemaining(participants);
        } else {
            ui.println("All property cards have been played.");
        }
        ui.showFinalScores(participants.stream()
                .map(GameParticipant::getPlayer)
                .sorted((a, b) -> Integer.compare(b.getTotalWealthThousands(), a.getTotalWealthThousands()))
                .toList());

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }

    /** Batches = most properties any player still holds at the start of Phase 2. */
    public static int countBatchesFromCardsWon(List<GameParticipant> participants) {
        int max = 0;
        for (GameParticipant participant : participants) {
            max = Math.max(max, participant.getPlayer().getProperties().size());
        }
        return Math.max(max, 1);
    }

    private boolean anyPlayerHasProperties() {
        for (GameParticipant participant : participants) {
            if (participant.getPlayer().hasProperties()) {
                return true;
            }
        }
        return false;
    }
}
