package forsale;

import java.util.List;

/**
 * Console input for bidding and selling.
 */
public class HumanController implements PlayerController, SellController {
    private final ConsoleUI ui;

    public HumanController(ConsoleUI ui) {
        this.ui = ui;
    }

    @Override
    public int decideBid(BidContext context) {
        return ui.readBidOrPass(
                context.getPlayer(),
                context.getMinimumBidThousands(),
                context.getHighestBidThousands(),
                context.isSoleBidder());
    }

    @Override
    public PropertyCard chooseProperty(SellContext context) {
        return ui.readPropertyChoice(context.getPlayer(), context);
    }
}
