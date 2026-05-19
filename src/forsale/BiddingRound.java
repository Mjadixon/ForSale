package forsale;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One bidding round: four cards on the table, bid until one player remains.
 */
public class BiddingRound {
    private static final int BID_INCREMENT = 1;

    private final ConsoleUI ui;
    private int bankedCash = 0;

    public BiddingRound(ConsoleUI ui) {
        this.ui = ui;
    }

    public int getBankedCash() {
        return bankedCash;
    }

    public void playRound(List<GameParticipant> participants, PropertyDeck deck, int roundNumber) {
        List<GameParticipant> activeBidders = new ArrayList<>(participants);
        List<PropertyCard> tableCards = deck.dealRound(GameRules.CARDS_PER_ROUND);
        int highestBid = 0;

        String dealt = tableCards.stream()
                .map(c -> "#" + c.getValue())
                .collect(Collectors.joining(", "));
        ui.logMove("Dealt: " + dealt);

        ui.clearPanel();
        ui.println("4 cards up for auction:");
        ui.showTable(tableCards);
        ui.showAllBalances(participants);

        boolean humanInRound = activeBidders.stream().anyMatch(GameParticipant::isHuman);
        if (humanInRound) {
            ui.pressEnterToContinue();
        }

        while (activeBidders.size() > 1) {
            for (int i = 0; i < activeBidders.size(); ) {
                if (tableCards.isEmpty()) {
                    break;
                }

                GameParticipant currentParticipant = activeBidders.get(i);
                Player current = currentParticipant.getPlayer();
                boolean soleBidder = activeBidders.size() == 1;

                ui.clearPanel();
                ui.println(currentParticipant.getLabel() + "'s turn");
                ui.showTable(tableCards);

                int minimumBid = highestBid + BID_INCREMENT;
                BidContext context = new BidContext(
                        current,
                        tableCards,
                        highestBid,
                        minimumBid,
                        activeBidders.size(),
                        soleBidder);

                int choice = soleBidder
                        ? forceWinningBid(current, minimumBid)
                        : currentParticipant.getController().decideBid(context);

                if (choice < 0) {
                    resolvePass(current, tableCards, activeBidders, i, participants);
                    if (activeBidders.size() <= 1) {
                        break;
                    }
                } else {
                    int previousCommitted = current.getCommittedBid();
                    current.setCommittedBid(choice);
                    highestBid = choice;
                    ui.logMove(current.getName() + " bids " + Currency.format(choice));
                    ui.clearPanel();
                    ui.println(current.getName() + " bids " + Player.formatMoney(choice));
                    ui.showAllBalances(participants);
                    i++;
                }
            }
        }

        if (activeBidders.isEmpty()) {
            ui.logMove("Round error: no bidder left");
            discardRemainingCards(tableCards);
            return;
        }

        if (tableCards.isEmpty()) {
            ui.logMove("Round error: no cards left for winner");
            return;
        }

        GameParticipant winnerParticipant = activeBidders.get(0);
        Player winner = winnerParticipant.getPlayer();
        PropertyCard wonCard = tableCards.remove(tableCards.size() - 1);
        winner.addProperty(wonCard);
        bankedCash += winner.payFullBidToBank();

        ui.logMove(winner.getName() + " wins #" + wonCard.getValue()
                + " pays " + Currency.format(highestBid));

        discardRemainingCards(tableCards);

        ui.clearPanel();
        ui.println(winner.getName() + " wins " + wonCard);
        ui.println("Pays " + Player.formatMoney(highestBid) + " to bank");
        ui.showAllBalances(participants);
    }

    /** Last bidder cannot pass — must hold the auction (min bid or existing commitment). */
    private int forceWinningBid(Player player, int minimumBid) {
        if (player.getCommittedBid() >= minimumBid) {
            return player.getCommittedBid();
        }
        if (player.getCash() + player.getCommittedBid() >= minimumBid) {
            return minimumBid;
        }
        return player.getCommittedBid() > 0 ? player.getCommittedBid() : minimumBid;
    }

    private void discardRemainingCards(List<PropertyCard> tableCards) {
        while (!tableCards.isEmpty()) {
            PropertyCard discarded = tableCards.remove(0);
            ui.logMove("Unused #" + discarded.getValue() + " discarded");
        }
    }

    private void resolvePass(
            Player player,
            List<PropertyCard> tableCards,
            List<GameParticipant> activeBidders,
            int index,
            List<GameParticipant> allParticipants) {
        if (tableCards.isEmpty()) {
            ui.logMove(player.getName() + " cannot pass (no cards)");
            return;
        }

        PropertyCard taken = tableCards.remove(0);
        player.addProperty(taken);

        int committedBeforeRefund = player.getCommittedBid();
        int refund = committedBeforeRefund / 2;
        bankedCash += player.refundHalfBid();

        ui.logMove(player.getName() + " pass #" + taken.getValue()
                + (refund > 0 ? " +" + Currency.format(refund) : ""));

        ui.clearPanel();
        ui.println(player.getName() + " passes");
        ui.println("Takes " + taken);
        if (refund > 0) {
            ui.println("Gets back " + Player.formatMoney(refund));
        }
        ui.showAllBalances(allParticipants);

        activeBidders.remove(index);
    }
}
