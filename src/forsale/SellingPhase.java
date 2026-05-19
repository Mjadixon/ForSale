package forsale;

import java.util.List;

/**
 * Phase 2: exactly five batches. Each batch ranks properties and awards four checks.
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
        ui.logMove("Phase 2: Selling begins");
        ui.clearPanel();
        ui.println("=== PHASE 2: SELLING ===");
        ui.println("5 batches of checks (same as 5 bidding rounds).");
        ui.println("Each batch: 4 checks on the table.");
        ui.println("Select a property; all reveal.");
        ui.println("Highest property rank wins the top check, and so on.");
        ui.println("Check amounts are added to your balance.");
        ui.println("Highest balance at the end wins.");

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }

        for (int batch = 1; batch <= GameRules.SELLING_ROUNDS; batch++) {
            ui.logMove("Sell batch " + batch + "/" + GameRules.SELLING_ROUNDS);
            roundRunner.playBatch(participants, checkDeck, batch);
        }

        ui.logMove("Phase 2 complete");
        ui.clearPanel();
        ui.println("=== SELLING DONE ===");
        ui.println("Final balances:");
        for (GameParticipant participant : participants) {
            ui.showWealthBreakdown(participant.getPlayer());
        }

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }
}
