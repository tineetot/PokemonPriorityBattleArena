// Starts the program, manages the main-menu loop, loads the dataset,
// and opens team selection.

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean colorsEnabled = !hasArgument(args, "--no-color")
                && System.getenv("NO_COLOR") == null;

        // Initialize the terminal UI with the specified color settings.
        TerminalUI terminalUI = new TerminalUI(colorsEnabled);
        Scanner scanner = new Scanner(System.in);
        PokemonDataLoader dataLoader = new PokemonDataLoader();
        boolean running = true;

        while (running) {
            terminalUI.showTitleScreen();
            terminalUI.showMainMenu();

            int selection = terminalUI.readMenuSelection(scanner);
            if (selection == 1) {
                startTeamSelection(dataLoader, terminalUI, scanner);
            } else {
                terminalUI.showFarewellMessage();
                running = false;
            }
        }
    }

    private static void startTeamSelection(PokemonDataLoader dataLoader,
            TerminalUI terminalUI, Scanner scanner) {
        try {
            Pokemon[] availablePokemon = dataLoader.loadDefaultDataset();
            terminalUI.runTeamSelection(scanner, availablePokemon);
        } catch (IOException | IllegalArgumentException exception) {
            terminalUI.showDatasetLoadError(exception.getMessage());
            terminalUI.waitForEnter(scanner);
        }
    }

    private static boolean hasArgument(String[] args, String expectedArgument) {
        for (String argument : args) {
            if (argument.equalsIgnoreCase(expectedArgument)) {
                return true;
            }
        }
        return false;
    }
}
