import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        boolean colorsEnabled = !hasArgument(args, "--no-color")
                && System.getenv("NO_COLOR") == null;

        AnsiTheme theme = new AnsiTheme(colorsEnabled);
        TerminalUI terminalUI = new TerminalUI(theme);

        terminalUI.showTitleScreen();
        terminalUI.showMainMenu();

        Scanner scanner = new Scanner(System.in);
        int selection = terminalUI.readMenuSelection(scanner);
        terminalUI.showSelectionMessage(selection);
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
