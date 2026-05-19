package forsale;

import java.util.List;

/**
 * Phase 2: each player gives a property; highest card wins the top check (added to total).
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
        ui.println("5 rounds. Each round:");
        ui.println("  1. Four checks are dealt (best check first).");
        ui.println("  2. Each player gives one property card face down.");
        ui.println("  3. All reveal. Highest property wins the top check.");
        ui.println("  4. That check is added to the winner's total.");
        ui.println("After all rounds, most money (checks + coins) wins.");

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }

        for (int round = 1; round <= GameRules.SELLING_ROUNDS; round++) {
            ui.logMove("Sell round " + round + "/" + GameRules.SELLING_ROUNDS);
            roundRunner.playRound(participants, checkDeck, round);
        }

        ui.logMove("Phase 2 complete");
        ui.clearPanel();
        ui.println("=== SELLING DONE ===");
        ui.println("Final money (checks earned + coins left):");
        for (GameParticipant participant : participants) {
            ui.showWealthBreakdown(participant.getPlayer());
        }

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }
}
