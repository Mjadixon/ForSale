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
            "  That winner starts bidding the next round.",
            "  End of each round: see who won which card.",
            "",
            "PHASE 2 — SELLING (1 batch per card you won)",
            "  Batches = how many properties you kept from Phase 1.",
            "  Each batch: 4 checks, pick 1 property, reveal by rank.",
            "  Rank 1 (highest #) wins the top check, etc.",
            "  After each batch -> next batch until all cards gone.",
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
            "",
            "After the game:",
            "  y / n     — restart or quit",
        };
    }

    /** Full rules + commands; press Enter to return to the game. */
    public static void showHelpMenu(GameDisplay ui) {
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

    public static void showOverview(GameDisplay ui) {
        showHelpMenu(ui);
    }

    public static void showBiddingHelp(GameDisplay ui) {
        showHelpMenu(ui);
    }

    private GameHelp() {}
}
