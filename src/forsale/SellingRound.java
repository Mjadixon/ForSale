package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One selling round: each player gives one property face down. On reveal, the
 * highest property wins the highest check (added to that player's total), and so on.
 */
public class SellingRound {
    private final ConsoleUI ui;

    public SellingRound(ConsoleUI ui) {
        this.ui = ui;
    }

    public void playRound(List<GameParticipant> participants, CheckDeck checkDeck, int roundNumber) {
        List<CheckCard> tableChecks = checkDeck.dealRound(GameRules.CARDS_PER_ROUND);

        String checks = tableChecks.stream()
                .map(c -> Currency.format(c.getValueGrands()))
                .collect(Collectors.joining(", "));
        ui.logMove("Checks on table: " + checks);

        ui.clearPanel();
        ui.println("Selling round " + roundNumber + "/" + GameRules.SELLING_ROUNDS);
        ui.println("Each player gives one property card face down.");
        ui.println("Checks up for grabs (high to low):");
        for (int i = 0; i < tableChecks.size(); i++) {
            ui.println("  " + (i + 1) + ". " + tableChecks.get(i));
        }
        ui.showAllCheckTotals(participants);

        boolean humanSelling = participants.stream().anyMatch(GameParticipant::isHuman);
        if (humanSelling) {
            ui.println();
            ui.println("Pick a property to give. Highest # wins the top check.");
            ui.pressEnterToContinue();
        }

        List<PropertyPlay> plays = collectFaceDownPlays(participants, tableChecks, roundNumber);
        if (plays.isEmpty()) {
            ui.logMove("No properties played");
            return;
        }

        ui.clearPanel();
        ui.println("--- All cards revealed ---");
        plays.sort(Comparator.comparingInt((PropertyPlay play) -> play.getProperty().getValue()).reversed());

        int awards = Math.min(plays.size(), tableChecks.size());
        for (int rank = 0; rank < awards; rank++) {
            PropertyPlay play = plays.get(rank);
            CheckCard checkWon = tableChecks.get(rank);
            Player player = play.getPlayer();
            player.addCheck(checkWon.getValueGrands());

            String awardLine = formatAward(rank, play, checkWon);
            ui.logMove(awardLine);
            ui.println(awardLine);
        }

        if (plays.size() > awards) {
            for (int rank = awards; rank < plays.size(); rank++) {
                PropertyPlay play = plays.get(rank);
                ui.println((rank + 1) + ". " + play.getPlayer().getName()
                        + " played " + play.getProperty() + " (no check left)");
            }
        }

        ui.println();
        ui.println("Checks won this round are added to each player's total.");
        ui.showAllCheckTotals(participants);

        if (participants.stream().anyMatch(GameParticipant::isHuman)) {
            ui.pressEnterToContinue();
        }
    }

    private List<PropertyPlay> collectFaceDownPlays(
            List<GameParticipant> participants, List<CheckCard> tableChecks, int roundNumber) {
        List<PropertyPlay> plays = new ArrayList<>();
        for (GameParticipant seller : participants) {
            Player player = seller.getPlayer();
            if (player.getProperties().isEmpty()) {
                ui.logMove(player.getName() + " has no card to give");
                continue;
            }

            SellContext context = new SellContext(player, tableChecks, roundNumber);
            PropertyCard given = seller.getSellController().chooseProperty(context);
            player.removeProperty(given);
            plays.add(new PropertyPlay(seller, given));

            if (seller.isHuman()) {
                ui.logMove("You gave a property face down");
            } else {
                ui.logMove(seller.getPlayer().getName() + " gave a property");
            }
        }
        return plays;
    }

    private String formatAward(int rank, PropertyPlay play, CheckCard checkWon) {
        String place = rank == 0 ? "HIGHEST card" : "rank " + (rank + 1);
        if (rank == 0) {
            return "WIN: " + play.getPlayer().getName() + " had the " + place
                    + " (" + play.getProperty() + ") and wins " + checkWon
                    + " -> total checks " + Currency.format(play.getPlayer().getCheckTotalThousands());
        }
        return (rank + 1) + ". " + play.getPlayer().getName() + " (" + play.getProperty()
                + ") wins " + checkWon;
    }
}
