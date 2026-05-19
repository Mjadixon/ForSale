package forsale;

/**
 * Console entry point for For Sale.
 */
public class Main {
    private static volatile boolean shouldRestart = false;

    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        ui.setRestartListener(createRestartListener());

        while (true) {
            shouldRestart = false;

            try {
                Game game = Game.setupFromConsole(ui);
                game.play(createRestartListener());
            } catch (RestartGameException e) {
                ui.logMove("--- Restarting game ---");
                continue;
            }

            if (shouldRestart) {
                ui.logMove("--- Restarting game ---");
                continue;
            }

            if (!Game.askPlayAgain(ui)) {
                ui.setRoundContext("Goodbye");
                ui.clearPanel();
                ui.println("Thanks for playing For Sale!");
                ui.refresh();
                break;
            }

            ui.logMove("--- New game ---");
        }
    }

    private static RestartListener createRestartListener() {
        return () -> shouldRestart = true;
    }
}

interface RestartListener {
    void onRestart();
}
