import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean colorsEnabled = !hasArgument(args, "--no-color")
                && System.getenv("NO_COLOR") == null;

        // Initialize the terminal UI and heap demo with the specified color settings.
        AnsiTheme theme = new AnsiTheme(colorsEnabled);
        TerminalUI terminalUI = new TerminalUI(theme);
        Scanner scanner = new Scanner(System.in);
        HeapDemo heapDemo = new HeapDemo(terminalUI);
        PokemonDataLoader dataLoader = new PokemonDataLoader();
        boolean running = true;

        while (running) {
            terminalUI.showTitleScreen();
            terminalUI.showMainMenu();

            int selection = terminalUI.readMenuSelection(scanner);
            if (selection == 1) {
                startTeamSelection(dataLoader, terminalUI, scanner);
            } else if (selection == 3) {
                heapDemo.run(scanner);
            } else if (selection == 5) {
                terminalUI.showSelectionMessage(selection);
                running = false;
            } else {
                terminalUI.showSelectionMessage(selection);
                terminalUI.waitForEnter(scanner);
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
