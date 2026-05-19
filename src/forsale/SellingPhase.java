package forsale;

import java.util.List;

/**
 * Phase 2: for each check on the table (high to low), players pick a property;
 * highest property wins that check. Continues until all properties are gone.
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
        ui.println("Checks go on the table from highest to lowest.");
        ui.println("For each check, pick one property you won in Phase 1.");
        ui.println("All reveal: highest property # wins that check.");
        ui.println("Keep going until every property card is gone.");
        ui.println("Highest balance (checks + coins) wins the game.");

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }

        int batch = 1;
        while (anyPlayerHasProperties() && checkDeck.remaining() > 0) {
            if (!roundRunner.playBatch(participants, checkDeck, batch)) {
                break;
            }
            batch++;
        }

        ui.logMove("Phase 2 complete — all properties sold");
        ui.clearPanel();
        ui.println("=== SELLING DONE ===");
        ui.println("All property cards have been played.");
        ui.println("Final balances:");
        for (GameParticipant participant : participants) {
            ui.showWealthBreakdown(participant.getPlayer());
        }

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
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
