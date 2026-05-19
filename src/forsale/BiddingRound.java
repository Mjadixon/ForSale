package forsale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * One bidding round: four cards on the table, bid until one player remains.
 * The auction winner starts bidding in the next round.
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

    /**
     * @param startingBidder who bids first this round (previous round's auction winner)
     * @return the auction winner (starts bidding next round)
     */
    public GameParticipant playRound(
            List<GameParticipant> participants,
            PropertyDeck deck,
            int roundNumber,
            GameParticipant startingBidder) {
        List<GameParticipant> activeBidders = orderStartingWith(participants, startingBidder);
        List<PropertyCard> tableCards = deck.dealRound(GameRules.CARDS_PER_ROUND);
        List<CardAward> cardsWonThisRound = new ArrayList<>();
        int highestBid = 0;

        String dealt = tableCards.stream()
                .map(c -> "#" + c.getValue())
                .collect(Collectors.joining(", "));
        ui.logMove("Dealt: " + dealt);

        ui.clearPanel();
        ui.println("=== Bidding round " + roundNumber + " of " + GameRules.BIDDING_ROUNDS + " ===");
        if (roundNumber > 1) {
            ui.println(startingBidder.getPlayer().getName()
                    + " starts bidding (won last round's auction).");
        } else {
            ui.println(startingBidder.getPlayer().getName() + " starts bidding.");
        }
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

                ui.setRoundContext("Phase 1 | Bid " + roundNumber + "/" + GameRules.BIDDING_ROUNDS
                        + " | " + current.getName());
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
                    resolvePass(current, tableCards, activeBidders, i, participants, cardsWonThisRound);
                    if (activeBidders.size() <= 1) {
                        break;
                    }
                } else {
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

        if (activeBidders.isEmpty() || tableCards.isEmpty()) {
            ui.logMove("Round error in round " + roundNumber);
            discardRemainingCards(tableCards);
            return startingBidder;
        }

        GameParticipant winnerParticipant = activeBidders.get(0);
        Player winner = winnerParticipant.getPlayer();
        PropertyCard wonCard = tableCards.remove(tableCards.size() - 1);
        winner.addProperty(wonCard);
        bankedCash += winner.payFullBidToBank();
        cardsWonThisRound.add(new CardAward(winner.getName(), wonCard, "auction winner"));

        discardRemainingCards(tableCards);
        showRoundCardSummary(roundNumber, cardsWonThisRound, winner, highestBid);
        ui.showAllBalances(participants);

        return winnerParticipant;
    }

    private void showRoundCardSummary(
            int roundNumber,
            List<CardAward> awards,
            Player auctionWinner,
            int highestBid) {
        awards.sort(Comparator.comparingInt((CardAward a) -> a.card.getValue()).reversed());

        ui.clearPanel();
        ui.println("--- Round " + roundNumber + ": who won which card ---");
        for (CardAward award : awards) {
            ui.println("  " + award.playerName + "  ->  " + award.card
                    + "  (" + award.how + ")");
        }
        ui.println();
        ui.println("Auction: " + auctionWinner.getName() + " wins highest card, pays "
                + Player.formatMoney(highestBid));
        ui.println(auctionWinner.getName() + " will start bidding next round.");

        for (CardAward award : awards) {
            ui.logMove("R" + roundNumber + " " + award.playerName + " got #" + award.card.getValue());
        }
        ui.logMove("R" + roundNumber + " " + auctionWinner.getName() + " starts next round");
    }

    private static List<GameParticipant> orderStartingWith(
            List<GameParticipant> participants, GameParticipant start) {
        int startIndex = participants.indexOf(start);
        if (startIndex < 0) {
            return new ArrayList<>(participants);
        }
        List<GameParticipant> ordered = new ArrayList<>();
        for (int i = 0; i < participants.size(); i++) {
            ordered.add(participants.get((startIndex + i) % participants.size()));
        }
        return ordered;
    }

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
            List<GameParticipant> allParticipants,
            List<CardAward> cardsWonThisRound) {
        if (tableCards.isEmpty()) {
            ui.logMove(player.getName() + " cannot pass (no cards)");
            return;
        }

        PropertyCard taken = tableCards.remove(0);
        player.addProperty(taken);
        cardsWonThisRound.add(new CardAward(player.getName(), taken, "passed"));

        int committedBeforeRefund = player.getCommittedBid();
        int refund = committedBeforeRefund / 2;
        bankedCash += player.refundHalfBid();

        ui.logMove(player.getName() + " pass, took #" + taken.getValue());

        ui.clearPanel();
        ui.println(player.getName() + " passes");
        ui.println("Takes " + taken);
        if (refund > 0) {
            ui.println("Gets back " + Player.formatMoney(refund));
        }
        ui.showAllBalances(allParticipants);

        activeBidders.remove(index);
    }

    private static final class CardAward {
        final String playerName;
        final PropertyCard card;
        final String how;

        CardAward(String playerName, PropertyCard card, String how) {
            this.playerName = playerName;
            this.card = card;
            this.how = how;
        }
    }
}
