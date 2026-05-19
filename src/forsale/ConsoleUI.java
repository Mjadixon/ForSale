package forsale;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console UI with a move log on the left and game content on the right.
 */
public class ConsoleUI {
    public static final int LOG_WIDTH = 44;

    private final Scanner scanner = new Scanner(System.in);
    private final MoveLog moveLog = new MoveLog(80);
    private final List<String> panelLines = new ArrayList<>();

    public void logMove(String move) {
        moveLog.add(move);
    }

    public void clearPanel() {
        panelLines.clear();
    }

    public void panelLine(String line) {
        panelLines.add(line);
    }

    public void println(String message) {
        panelLine(message);
        refresh();
    }

    public void println() {
        panelLine("");
        refresh();
    }

    public void printDivider() {
        panelLine("------------------------------------");
        refresh();
    }

    public void refresh() {
        clearScreen();
        List<String> moves = new ArrayList<>(moveLog.snapshot());
        moves.add(0, "-- Moves --");

        int rows = Math.max(moves.size(), panelLines.size() + 1);
        for (int i = 0; i < rows; i++) {
            String left = i < moves.size() ? truncate(padRight(moves.get(i), LOG_WIDTH), LOG_WIDTH) : padRight("", LOG_WIDTH);
            String right;
            if (i == 0) {
                right = padRight("-- Game --", 42);
            } else {
                int bodyIndex = i - 1;
                right = bodyIndex < panelLines.size() ? panelLines.get(bodyIndex) : "";
            }
            System.out.println(left + " | " + right);
        }
        System.out.flush();
    }

    /**
     * Reads a line; if allowHelp and user types help, opens help menu and reads again.
     */
    public String readLineAllowHelp(boolean allowHelp) {
        while (true) {
            String line = scanner.nextLine().trim();
            if (allowHelp && line.equalsIgnoreCase("help")) {
                GameHelp.showHelpMenu(this);
                continue;
            }
            return line;
        }
    }

    public void pressEnterToContinue() {
        while (true) {
            panelLine("");
            panelLine("Press Enter to continue (type help for rules)...");
            refresh();
            String line = readLineAllowHelp(true);
            if (line.isEmpty()) {
                return;
            }
            logMove("Press Enter to continue");
        }
    }

    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            clearPanel();
            panelLine(prompt + " (" + min + "-" + max + ", help):");
            refresh();

            String line = readLineAllowHelp(true);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            logMove("Invalid number");
        }
    }

    public String readNonEmptyLine(String prompt) {
        while (true) {
            clearPanel();
            panelLine(prompt + " (help):");
            refresh();

            String line = readLineAllowHelp(true);
            if (!line.isEmpty()) {
                return line;
            }
            logMove("Empty name rejected");
        }
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            clearPanel();
            panelLine(prompt + " (y/n, help):");
            refresh();

            String answer = readLineAllowHelp(true).toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            logMove("Need y or n");
        }
    }

    public int readBidOrPass(Player player, int minimumBidThousands, int highestBidThousands, boolean soleBidder) {
        while (true) {
            if (soleBidder) {
                return minimumBidThousands;
            }

            clearPanel();
            panelLine("Your coins: " + Player.formatMoney(player.getCash()));
            if (player.getCommittedBid() > 0) {
                panelLine("Committed this round: " + Player.formatMoney(player.getCommittedBid()));
            }
            if (highestBidThousands > 0) {
                panelLine("Current high bid: " + Player.formatMoney(highestBidThousands));
            }
            panelLine("Next bid: " + Player.formatMoney(minimumBidThousands));
            panelLine("");
            panelLine("Enter     = bid " + Player.formatMoney(minimumBidThousands));
            panelLine("0 / pass  = pass");
            panelLine("number    = higher total bid (thousands)");
            panelLine("help      = rules menu");
            refresh();

            String line = readLineAllowHelp(true).toLowerCase();

            if (line.isEmpty()) {
                if (canAffordBid(player, minimumBidThousands)) {
                    return minimumBidThousands;
                }
                logMove("Cannot afford " + Currency.format(minimumBidThousands));
                continue;
            }

            if (line.equals("pass") || line.equals("0")) {
                return -1;
            }

            int input;
            try {
                input = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                logMove("Invalid input");
                continue;
            }

            if (input == 0) {
                return -1;
            }

            if (input < minimumBidThousands) {
                logMove("Below minimum bid");
                continue;
            }

            if (!canAffordBid(player, input)) {
                logMove("Cannot afford bid");
                continue;
            }

            return input;
        }
    }

    private static boolean canAffordBid(Player player, int totalBidThousands) {
        return totalBidThousands - player.getCommittedBid() <= player.getCash();
    }

    public void showTable(List<PropertyCard> tableCards) {
        panelLine("Table (low -> high):");
        for (int i = 0; i < tableCards.size(); i++) {
            panelLine("  " + (i + 1) + ". " + tableCards.get(i));
        }
        refresh();
    }

    public void showAllCheckTotals(List<GameParticipant> participants) {
        panelLine("--- Check earnings ---");
        for (GameParticipant participant : participants) {
            Player player = participant.getPlayer();
            panelLine("  " + player.getName() + ": "
                    + Player.formatMoney(player.getCheckTotalThousands()));
        }
        refresh();
    }

    /** Coins + checks = full balance for every player. */
    public void showAllBalances(List<GameParticipant> participants) {
        panelLine("--- Balances ---");
        for (GameParticipant participant : participants) {
            Player player = participant.getPlayer();
            panelLine("  " + player.getName() + ": "
                    + Player.formatMoney(player.getTotalWealthThousands())
                    + "  (coins " + Player.formatMoney(player.getCash())
                    + ", checks " + Player.formatMoney(player.getCheckTotalThousands()) + ")");
        }
        refresh();
    }

    public void showPlayerSummary(Player player) {
        StringBuilder props = new StringBuilder();
        for (PropertyCard card : player.getProperties()) {
            if (props.length() > 0) {
                props.append(", ");
            }
            props.append(card.getValue());
        }
        if (props.length() == 0) {
            props.append("(none)");
        }
        println(player.getName() + " | Coins: " + Player.formatMoney(player.getCash())
                + " | Props: " + props);
    }

    public PropertyCard readPropertyChoice(Player player, SellContext context) {
        while (true) {
            clearPanel();
            panelLine("Batch " + context.getBatchNumber() + " of " + GameRules.SELLING_ROUNDS);
            panelLine("Checks this batch (rank 1 = best):");
            List<CheckCard> checks = context.getTableChecks();
            for (int i = 0; i < checks.size(); i++) {
                panelLine("  Rank " + (i + 1) + ": " + checks.get(i));
            }
            panelLine("");
            panelLine("Your balance: " + Player.formatMoney(player.getTotalWealthThousands()));
            panelLine("Your properties (pick one to play):");
            List<PropertyCard> properties = player.getPropertiesHighToLow();
            for (int i = 0; i < properties.size(); i++) {
                panelLine("  " + (i + 1) + ". " + properties.get(i));
            }
            panelLine("");
            panelLine("Enter number to select (1 = highest):");
            panelLine("help = rules menu");
            refresh();

            String line = readLineAllowHelp(true).toLowerCase();
            if (line.isEmpty()) {
                logMove("Pick a property number");
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                logMove("Invalid property #");
                continue;
            }

            if (choice >= 1 && choice <= properties.size()) {
                return properties.get(choice - 1);
            }
            logMove("Invalid property #");
        }
    }

    public void showWealthBreakdown(Player player) {
        StringBuilder checkList = new StringBuilder();
        for (int check : player.getChecks()) {
            if (checkList.length() > 0) {
                checkList.append(", ");
            }
            checkList.append(Player.formatMoney(check));
        }
        if (checkList.length() == 0) {
            checkList.append("(none)");
        }

        println(player.getName() + ":");
        println("  Checks earned: " + checkList);
        println("  Coins left:    " + Player.formatMoney(player.getCash()));
        println("  Balance:       " + Player.formatMoney(player.getTotalWealthThousands()));
    }

    private static void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text;
        }
        return text + " ".repeat(width - text.length());
    }

    private static String truncate(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        return text.substring(0, width - 1) + "...";
    }
}
