package forsale;

import java.util.List;

/**
 * Phase 1: exactly five rounds, four property cards per round.
 */
public class BiddingPhase {
    private final GameDisplay ui;
    private final List<GameParticipant> participants;
    private final PropertyDeck deck;
    private final BiddingRound roundRunner;

    public BiddingPhase(GameDisplay ui, List<GameParticipant> participants, PropertyDeck deck) {
        this.ui = ui;
        this.participants = participants;
        this.deck = deck;
        this.roundRunner = new BiddingRound(ui);
    }

    public void play() {
        ui.setRoundContext("Phase 1 | Bidding");
        ui.logMove("Phase 1: Bidding begins");
        ui.clearPanel();
        ui.println("=== PHASE 1: BIDDING ===");
        ui.println(
                GameRules.BIDDING_ROUNDS + " rounds, " + GameRules.CARDS_PER_ROUND + " cards each (one per player).");
        ui.println("20 total property cards: #1–20 (higher value = more desirable).");
        ui.println("Start: " + Player.formatMoney(Game.STARTING_CASH_THOUSANDS));
        ui.println("Pass → lowest card, half bid back.");
        ui.println("Last bidder → highest card, full bid.");
        ui.println("Auction winner starts bidding the next round.");

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }

        GameParticipant nextRoundStarter = participants.get(0);
        for (int round = 1; round <= GameRules.BIDDING_ROUNDS; round++) {
            ui.setRoundContext("Phase 1 | Bid round " + round + "/" + GameRules.BIDDING_ROUNDS);
            nextRoundStarter = roundRunner.playRound(participants, deck, round, nextRoundStarter);

            if (round < GameRules.BIDDING_ROUNDS
                    && participants.stream().anyMatch(GameParticipant::isHuman)) {
                ui.pressEnterToContinue();
            }
        }

        ui.logMove("Phase 1 complete");
        ui.clearPanel();
        ui.println("=== BIDDING DONE ===");
        ui.println("Bank took: " + Player.formatMoney(roundRunner.getBankedCash()));
        ui.println();
        ui.println("Properties won:");
        for (GameParticipant participant : participants) {
            ui.showPlayerSummary(participant.getPlayer());
        }

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }
}
