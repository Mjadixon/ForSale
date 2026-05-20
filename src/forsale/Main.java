package forsale;

/**
 * Console entry point for For Sale game.
 * 
 * Manages the main game loop including:
 * - Game setup and initialization
 * - Game execution with restart capability
 * - Play again prompt and graceful shutdown
 */
public class Main {
    /** Flag set when player enters 'restart' during gameplay */
    private static volatile boolean shouldRestart = false;

    /**
     * Main entry point. Runs the game loop until player chooses to quit.
     * 
     * Game flow:
     * 1. Initialize console UI and restart listener
     * 2. Loop: Setup → Play → Prompt for restart/new game/quit
     * 3. Handle restart requests (in-game 'restart' command)
     * 4. Handle play again prompt (after game completion)
     * 5. Exit gracefully on quit
     * 
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        // Initialize the console UI for displaying game state and reading input
        ConsoleUI ui = new ConsoleUI();
        ui.setRestartListener(createRestartListener());

        // Main game loop - continues until player chooses to quit
        while (true) {
            // Reset restart flag at start of each game
            shouldRestart = false;

            try {
                // Setup new game with player configuration
                Game game = Game.setupFromConsole(ui);
                
                // Play the game - may throw RestartGameException if player types 'restart'
                game.play(createRestartListener());
                
            } catch (RestartGameException e) {
                // Player typed 'restart' during gameplay - restart immediately
                ui.logMove("--- Restarting game ---");
                continue;
            }

            // Check if restart flag was set (fallback mechanism)
            if (shouldRestart) {
                ui.logMove("--- Restarting game ---");
                continue;
            }

            // Game completed normally - ask if player wants to play again
            // Clear screen and show prompt with visual separation
            ui.setRoundContext("Game Over");
            ui.clearPanel();
            ui.printDivider();
            ui.println("");
            
            // Ask user if they want to play again
            if (!Game.askPlayAgain(ui)) {
                // Player chose to quit - show goodbye message and exit
                ui.setRoundContext("Goodbye");
                ui.clearPanel();
                ui.println("=====================================");
                ui.println("Thanks for playing For Sale!");
                ui.println("=====================================");
                ui.refresh();
                break;
            }

            // Player wants new game - log and loop back to setup
            ui.logMove("--- New game ---");
        }
    }

    /**
     * Creates a listener that sets the restart flag when invoked.
     * This callback is used when player types 'restart' during gameplay.
     * 
     * @return RestartListener that sets the shouldRestart flag
     */
    private static RestartListener createRestartListener() {
        return () -> shouldRestart = true;
    }
}

interface RestartListener {
    void onRestart();
}
