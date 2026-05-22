package forsale;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SwingUI extends JFrame implements GameDisplay {
    private static final int FRAME_WIDTH = 1400;
    private static final int FRAME_HEIGHT = 800;

    private JLabel roundHeaderLabel;
    private JTextArea gameContentArea;
    private JTextArea moveLogArea;
    private JPanel inputPanel;
    private JPanel buttonPanel;
    private RestartListener restartListener;
    private List<String> panelLines = new ArrayList<>();

    public SwingUI() {
        setTitle("For Sale - Game UI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(FRAME_WIDTH, FRAME_HEIGHT);
        setLocationRelativeTo(null);
        setResizable(true);

        setupUI();
        setVisible(true);
    }

    private void setupUI() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        roundHeaderLabel = new JLabel("For Sale");
        roundHeaderLabel.setFont(new Font("Arial", Font.BOLD, 24));
        roundHeaderLabel.setBackground(new Color(220, 220, 220));
        roundHeaderLabel.setOpaque(true);
        roundHeaderLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        main.add(roundHeaderLabel, BorderLayout.NORTH);

        // Center - Log on left, game content on right
        JPanel centerPanel = new JPanel(new BorderLayout(10, 5));

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Move Log"));
        moveLogArea = new JTextArea(25, 30);
        moveLogArea.setEditable(false);
        moveLogArea.setFont(new Font("Monospaced", Font.PLAIN, 10));
        moveLogArea.setLineWrap(true);
        JScrollPane logScroll = new JScrollPane(moveLogArea);
        logPanel.add(logScroll, BorderLayout.CENTER);
        centerPanel.add(logPanel, BorderLayout.WEST);

        JPanel contentPanel = new JPanel(new BorderLayout(5, 5));
        gameContentArea = new JTextArea(25, 40);
        gameContentArea.setEditable(false);
        gameContentArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gameContentArea.setLineWrap(true);
        gameContentArea.setWrapStyleWord(true);
        JScrollPane contentScroll = new JScrollPane(gameContentArea);
        contentPanel.add(contentScroll, BorderLayout.CENTER);
        centerPanel.add(contentPanel, BorderLayout.CENTER);

        main.add(centerPanel, BorderLayout.CENTER);

        // Input panel
        inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Input"));

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(buttonPanel, BorderLayout.CENTER);

        main.add(inputPanel, BorderLayout.SOUTH);
        setContentPane(main);
    }

    @Override
    public void setRoundContext(String label) {
        SwingUtilities.invokeLater(() -> roundHeaderLabel.setText(label));
        logMove(">> " + label);
    }

    @Override
    public void logMove(String move) {
        SwingUtilities.invokeLater(() -> {
            moveLogArea.append(move + "\n");
            moveLogArea.setCaretPosition(moveLogArea.getDocument().getLength());
        });
    }

    @Override
    public void setRestartListener(RestartListener listener) {
        this.restartListener = listener;
    }

    @Override
    public void clearPanel() {
        panelLines.clear();
    }

    @Override
    public void panelLine(String line) {
        panelLines.add(line);
    }

    @Override
    public void println(String message) {
        panelLine(message);
        refresh();
    }

    @Override
    public void println() {
        panelLine("");
        refresh();
    }

    @Override
    public void printDivider() {
        panelLine("------------------------------------");
        refresh();
    }

    @Override
    public void refresh() {
        SwingUtilities.invokeLater(() -> {
            StringBuilder sb = new StringBuilder();
            for (String line : panelLines) {
                sb.append(line).append("\n");
            }
            gameContentArea.setText(sb.toString());
            gameContentArea.setCaretPosition(0);
        });
    }

    @Override
    public void showTable(List<PropertyCard> tableCards) {
        panelLine("Table (low -> high):");
        for (int i = 0; i < tableCards.size(); i++) {
            panelLine("  " + (i + 1) + ". " + tableCards.get(i));
        }
        refresh();
    }

    @Override
    public void showAllBalances(List<GameParticipant> participants) {
        panelLine("--- Balances ---");
        for (GameParticipant participant : participants) {
            Player player = participant.getPlayer();
            panelLine("  " + player.getName() + ": "
                    + Player.formatMoney(player.getTotalWealthThousands())
                    + "  (coins " + Player.formatMoney(player.getCash())
                    + " + checks " + Player.formatMoney(player.getCheckTotalThousands()) + ")");
        }
        refresh();
    }

    @Override
    public void showPropertiesRemaining(List<GameParticipant> participants) {
        panelLine("--- Cards left to sell ---");
        for (GameParticipant participant : participants) {
            Player player = participant.getPlayer();
            StringBuilder props = new StringBuilder();
            for (PropertyCard card : player.getPropertiesHighToLow()) {
                if (props.length() > 0) {
                    props.append(", ");
                }
                props.append("#").append(card.getValue());
            }
            if (props.length() == 0) {
                props.append("(none)");
            }
            panelLine("  " + player.getName() + ": " + props);
        }
        refresh();
    }

    @Override
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

    @Override
    public void showFinalScores(List<Player> ranked) {
        setRoundContext("FINAL SCORES");
        clearPanel();
        panelLine("=== FINAL SCORES ===");
        panelLine("Total balance = coins + all checks won");
        panelLine("");
        panelLine(String.format("%-4s %-14s %-12s %-12s %-12s",
                "#", "Player", "Checks", "Coins", "TOTAL"));
        panelLine("---- -------------- ------------ ------------ ------------");

        int rank = 1;
        for (Player player : ranked) {
            panelLine(String.format("%-4d %-14s %-12s %-12s %-12s",
                    rank++,
                    truncateName(player.getName(), 14),
                    Player.formatMoney(player.getCheckTotalThousands()),
                    Player.formatMoney(player.getCash()),
                    Player.formatMoney(player.getTotalWealthThousands())));
        }
        refresh();
    }

    @Override
    public String readLineAllowHelp(boolean allowHelp) {
        String prompt = allowHelp
                ? "Enter (help/restart available):"
                : "Enter:";
        String input = blockingTextInput(prompt);

        if (allowHelp && input.equalsIgnoreCase("restart")) {
            if (restartListener != null) {
                restartListener.onRestart();
            }
            throw new RestartGameException();
        }

        if (allowHelp && input.equalsIgnoreCase("help")) {
            GameHelp.showHelpMenu(this);
            return readLineAllowHelp(allowHelp);
        }

        return input;
    }

    @Override
    public void pressEnterToContinue() {
        blockingTextInput("Press Enter to continue (help for rules)...");
    }

    @Override
    public int readIntInRange(String prompt, int min, int max) {
        while (true) {
            String input = blockingTextInput(prompt + " (" + min + "-" + max + "):");
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                logMove("Invalid number");
            } catch (NumberFormatException e) {
                logMove("Invalid input");
            }
        }
    }

    @Override
    public String readNonEmptyLine(String prompt) {
        while (true) {
            String input = blockingTextInput(prompt + ":");
            if (!input.trim().isEmpty()) {
                return input.trim();
            }
            logMove("Empty input rejected");
        }
    }

    @Override
    public boolean readYesNo(String prompt) {
        while (true) {
            String input = blockingTextInput(prompt + " (y/n)?");
            String lower = input.toLowerCase().trim();
            if (lower.equals("y") || lower.equals("yes")) {
                return true;
            }
            if (lower.equals("n") || lower.equals("no")) {
                return false;
            }
            logMove("Enter y or n");
        }
    }

    @Override
    public int readBidOrPass(Player player, int minimumBidThousands, int highestBidThousands, boolean soleBidder) {
        if (soleBidder) {
            return minimumBidThousands;
        }

        while (true) {
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

    @Override
    public PropertyCard readPropertyChoice(Player player, SellContext context) {
        while (true) {
            clearPanel();
            panelLine("Batch " + context.getBatchNumber() + " of " + context.getTotalBatches());
            panelLine("Checks this batch (rank 1 = best):");
            List<CheckCard> checks = context.getTableChecks();
            for (int i = 0; i < checks.size(); i++) {
                panelLine("  Rank " + (i + 1) + ": " + checks.get(i));
            }
            panelLine("");
            panelLine("Balance: " + Player.formatMoney(player.getTotalWealthThousands()));
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

    private String blockingTextInput(String prompt) {
        BlockingQueue<String> resultQueue = new LinkedBlockingQueue<>();
        try {
            SwingUtilities.invokeLater(() -> showTextInputDialog(prompt, resultQueue));
            return resultQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    private void showTextInputDialog(String prompt, BlockingQueue<String> resultQueue) {
        JDialog dialog = new JDialog(this, "Input", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(500, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel promptLabel = new JLabel(prompt);
        panel.add(promptLabel, BorderLayout.NORTH);

        JTextField field = new JTextField();
        panel.add(field, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("OK");
        JButton helpBtn = new JButton("Help");

        Runnable submitAction = () -> {
            try {
                resultQueue.put(field.getText());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            dialog.dispose();
        };

        okBtn.addActionListener(e -> submitAction.run());

        helpBtn.addActionListener(e -> {
            GameHelp.showHelpMenu(this);
        });

        field.addActionListener(e -> submitAction.run());

        buttonPanel.add(helpBtn);
        buttonPanel.add(okBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(panel);
        dialog.getRootPane().setDefaultButton(okBtn);

        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                field.requestFocus();
            }
        });

        dialog.setVisible(true);
    }

    private static boolean canAffordBid(Player player, int totalBidThousands) {
        return totalBidThousands - player.getCommittedBid() <= player.getCash();
    }

    private static String truncateName(String name, int max) {
        if (name.length() <= max) {
            return name;
        }
        return name.substring(0, max - 1) + ".";
    }
}
