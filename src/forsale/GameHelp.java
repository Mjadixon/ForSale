package forsale;

/**
 * How to play and input commands (shown in-game and mirrored in README).
 */
public final class GameHelp {
    public static final String TITLE = "FOR SALE — How to Play";

    public static String[] overviewLines() {
        return new String[] {
            TITLE,
            "",
            "GOAL: Highest balance at the end (checks + coins).",
            "",
            "SETUP: 4 players (you pick human or AI for each).",
            "Everyone starts with " + Currency.format(Game.STARTING_CASH_THOUSANDS) + ".",
            "",
            "PHASE 1 — BIDDING (5 rounds)",
            "  Each round: 4 property cards dealt (higher # = better).",
            "  Take turns bidding until one player remains.",
            "  PASS (0): take the lowest card left; get half your bid back.",
            "  LAST BIDDER: takes the highest card; pays full bid to bank.",
            "",
            "PHASE 2 — SELLING (until all cards gone)",
            "  Checks on table, highest value first.",
            "  For EACH check: pick a property you won.",
            "  All reveal — highest property # wins that check.",
            "  Next check, then next, until cards run out.",
            "  Winner = highest balance (checks + coins).",
            "",
            "Left column = move log. Right column = current screen.",
        };
    }

    public static String[] commandLines() {
        return new String[] {
            "COMMANDS",
            "",
            "Setup:",
            "  y / n     — human or AI for this seat",
            "  name      — your display name (humans only)",
            "",
            "Bidding (your turn):",
            "  Enter     — bid the minimum (beats last bid by $1,000)",
            "  0         — pass",
            "  pass      — pass",
            "  <number>  — total bid in thousands (3 = " + Currency.format(3) + ")",
            "  help      — show bidding help again",
            "",
            "Selling (your turn):",
            "  <number>  — pick property (1 = your highest card)",
            "",
            "Anywhere:",
            "  Enter     — continue when asked",
        };
    }

    public static void showOverview(ConsoleUI ui) {
        ui.logMove("Help shown");
        ui.clearPanel();
        for (String line : overviewLines()) {
            ui.panelLine(line);
        }
        ui.panelLine("");
        for (String line : commandLines()) {
            ui.panelLine(line);
        }
        ui.refresh();
        ui.pressEnterToContinue();
    }

    public static void showBiddingHelp(ConsoleUI ui) {
        ui.clearPanel();
        ui.panelLine("Bidding help:");
        ui.panelLine("  Enter  → minimum bid shown on screen");
        ui.panelLine("  0      → pass");
        ui.panelLine("  pass   → pass");
        ui.panelLine("  number → total bid (3 = " + Currency.format(3) + ")");
        ui.refresh();
    }

    private GameHelp() {}
}
