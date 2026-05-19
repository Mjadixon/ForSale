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
            "PHASE 2 — SELLING (5 batches)",
            "  Each batch: 4 checks on the table (best first).",
            "  Pick one property you won in Phase 1.",
            "  All reveal; ranks by property # (high to low).",
            "  Rank 1 wins the top check, rank 2 the next, etc.",
            "  Check value is added to your balance.",
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
            "  0 / pass  — pass",
            "  <number>  — total bid in thousands (3 = " + Currency.format(3) + ")",
            "  help      — open this help menu",
            "",
            "Selling (your turn):",
            "  <number>  — pick property by list # (1 = highest)",
            "  help      — open this help menu",
            "",
            "Anywhere:",
            "  help      — open help, then continue playing",
            "  Enter     — continue when asked",
        };
    }

    /** Full rules + commands; press Enter to return to the game. */
    public static void showHelpMenu(ConsoleUI ui) {
        ui.logMove("Help menu opened");
        ui.clearPanel();
        for (String line : overviewLines()) {
            ui.panelLine(line);
        }
        ui.panelLine("");
        for (String line : commandLines()) {
            ui.panelLine(line);
        }
        ui.panelLine("");
        ui.panelLine("Press Enter to return to the game...");
        ui.refresh();
        ui.readLineAllowHelp(false);
    }

    public static void showOverview(ConsoleUI ui) {
        showHelpMenu(ui);
    }

    public static void showBiddingHelp(ConsoleUI ui) {
        showHelpMenu(ui);
    }

    private GameHelp() {}
}
