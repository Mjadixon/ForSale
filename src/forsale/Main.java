package forsale;

/**
 * Console entry point for For Sale.
 */
public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();

        while (true) {
            Game game = Game.setupFromConsole(ui);
            game.play();

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
}
