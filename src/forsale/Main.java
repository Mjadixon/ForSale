package forsale;

/**
 * Console entry point for For Sale (2 phases, 5 rounds each).
 */
public class Main {
    public static void main(String[] args) {
        ConsoleUI ui = new ConsoleUI();
        Game game = Game.setupFromConsole(ui);
        game.play();
    }
}
