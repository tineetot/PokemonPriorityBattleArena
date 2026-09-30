// Displays the menus, team-selection screens, prompts, errors, and other terminal content.

import java.util.Scanner;

public final class TerminalUI {
    // Display dimensions.
    private static final int BODY_WIDTH = 68;
    private static final int SCREEN_WIDTH = 60;
    private static final int SCREEN_MARGIN = 3;
    private static final int MINIMUM_MENU_CHOICE = 1;
    private static final int MAXIMUM_MENU_CHOICE = 2;

    private static final String GAME_TITLE = "PRIORITY BATTLE ARENA";

    // Game Boy-inspired artwork.
    private static final String[] POKEMON_LOGO = {
        " ____   ___  _  __ _____ __  __  ___  _   _ ",
        "|  _ \\ / _ \\| |/ /| ____|  \\/  |/ _ \\| \\ | |",
        "| |_) | | | | ' / |  _| | |\\/| | | | |  \\| |",
        "|  __/| |_| | . \\ | |___| |  | | |_| | |\\  |",
        "|_|    \\___/|_|\\_\\|_____|_|  |_|\\___/|_| \\_|"
    };

    private static final String[] BATTLE_SCENE = {
        "      .--.                                        .--.      ",
        "   .-(____)-.                                  .-(____)-.   ",
        "      /\\             *     .----.     *             /\\      ",
        "     /  \\                 /      \\                 /  \\     ",
        "    /____\\               |---()---|               /____\\    ",
        "      ||                  \\      /                  ||      ",
        "  ___/||\\___              '----'              ___/||\\___  ",
        "^^^..^^^..^^^..^^^..^^^..^^^....^^^..^^^..^^^..^^^..^^^..^^^"
    };

    private static final String[] D_PAD = {
        "   [^]   ",
        "[<][+][>]",
        "   [v]   "
    };

    private static final String[] SPEAKER = {
        ". . . .",
        " . . . ."
    };

    // ANSI color palette.
    private static final String ESCAPE = "\u001B[";
    private static final String CORAL_RED = foreground(242, 85, 99);
    private static final String BERRY_RED = foreground(201, 60, 84);
    private static final String CHERRY_WINE = foreground(187, 58, 77);
    private static final String CREAM_PEACH = foreground(251, 210, 176);
    private static final String DUSTY_ROSE = foreground(229, 165, 146);
    private static final String WARM_ROSE = foreground(212, 135, 123);
    private static final String SOFT_WHITE = foreground(255, 244, 236);
    private static final String WARM_GRAY = foreground(207, 194, 188);
    private static final String BOLD = ESCAPE + "1m";
    private static final String RESET = ESCAPE + "0m";

    private final boolean colorsEnabled;

    public TerminalUI(boolean colorsEnabled) {
        this.colorsEnabled = colorsEnabled;
    }

    public void showTitleScreen() {
        printBodyTop();
        printBodyLine("", 0);
        printPowerIndicator();
        printScreenBorder();
        printScreenLine("", 0);
        for (String line : POKEMON_LOGO) {
            printCenteredScreenLine(coral(line), line.length());
        }
        printCenteredScreenLine(boldCream(GAME_TITLE), GAME_TITLE.length());
        printScreenLine("", 0);
        for (String line : BATTLE_SCENE) {
            printCenteredScreenLine(gray(line), line.length());
        }
    }

    public void showMainMenu() {
        printScreenDivider();
        printCenteredScreenLine(boldWhite("MAIN MENU"), 9);
        printScreenMenuItem(1, "Start Battle");
        printScreenMenuItem(2, "Exit");
        printScreenLine("", 0);
        printScreenBorder();
        printBodyLine("", 0);
        printPhysicalControls();
        printSelectAndStart();
        printSpeaker();
        printBodyLine("", 0);
        printBodyBottom();
    }

    public Pokemon[] runTeamSelection(Scanner scanner, Pokemon[] availablePokemon) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null.");
        }

        TeamSelection selection = new TeamSelection(availablePokemon);
        String statusMessage = "Choose your first Pokemon.";
        boolean showError = false;

        while (!selection.isFull()) {
            showTeamSelectionScreen(
                    availablePokemon, selection, statusMessage, showError);
            int pokemonNumber = readTeamSelectionNumber(scanner, availablePokemon.length);
            if (pokemonNumber == 0) {
                return null;
            }

            try {
                selection.addPokemon(pokemonNumber - 1);
                statusMessage = "Selected "
                        + availablePokemon[pokemonNumber - 1].getName() + ".";
                showError = false;
            } catch (IllegalArgumentException | IllegalStateException exception) {
                statusMessage = exception.getMessage();
                showError = true;
            }
        }

        Pokemon[] completedTeam = selection.getSelectedTeam();
        showTeamConfirmation(completedTeam);
        waitForEnter(scanner);
        return completedTeam;
    }

    public void showDatasetLoadError(String errorMessage) {
        String detail = errorMessage;
        if (detail == null || detail.trim().isEmpty()) {
            detail = "The dataset could not be loaded.";
        }
        System.out.println();
        System.out.println(coral("  Dataset loading error:"));
        System.out.println(gray("  " + detail));
    }

    public int readMenuSelection(Scanner scanner) {
        while (true) {
            System.out.print(cream("  Select an option [1-2]: "));

            if (!scanner.hasNextLine()) {
                System.out.println();
                return MAXIMUM_MENU_CHOICE;
            }

            String input = scanner.nextLine().trim();

            try {
                int selection = Integer.parseInt(input);
                if (selection >= MINIMUM_MENU_CHOICE && selection <= MAXIMUM_MENU_CHOICE) {
                    return selection;
                }
            } catch (NumberFormatException exception) {
                // The shared message below handles non-numeric input too.
            }

            System.out.println(coral(
                    "  Invalid choice. Please enter a number from 1 to 2."));
        }
    }

    private void showTeamSelectionScreen(Pokemon[] availablePokemon,
            TeamSelection selection, String statusMessage, boolean showError) {
        System.out.println();
        printBodyTop();
        printPowerIndicator();
        printScreenBorder();
        printScreenLine("", 0);
        printCenteredScreenLine(boldCream("CHOOSE YOUR TEAM"), 16);
        printCenteredScreenLine(gray("Select 3 unique Pokemon"), 23);
        printScreenDivider();
        String listHeader = "#   NAME         TYPE       HP    SPD";
        printContentPageLine(gray(listHeader), listHeader.length());

        for (int index = 0; index < availablePokemon.length; index++) {
            Pokemon pokemon = availablePokemon[index];
            String line = "[" + (index + 1) + "] "
                    + padRight(pokemon.getName(), 11) + " "
                    + padRight(pokemon.getType().toString(), 8)
                    + "  " + padLeft(pokemon.getMaxHp(), 3)
                    + "   " + padLeft(pokemon.getSpeed(), 3);
            printContentPageLine(white(line), line.length());
        }

        printScreenDivider();
        String styledStatus = showError
                ? coral(statusMessage) : cream(statusMessage);
        printContentPageLine(styledStatus, statusMessage.length());
        String teamLine = "Team: " + selection.getSelectedCount()
                + "/" + TeamSelection.TEAM_SIZE + "  "
                + selectedNames(selection.getSelectedTeam(), selection.getSelectedCount());
        printContentPageLine(white(teamLine), teamLine.length());
        printContentPageLine(gray("[0] Back to Main Menu"), 21);
        printScreenLine("", 0);
        printScreenBorder();
        printPhysicalControls();
        printSelectAndStart();
        printCompactSpeaker();
        printBodyBottom();
    }

    private int readTeamSelectionNumber(Scanner scanner, int pokemonCount) {
        while (true) {
            System.out.print(cream(
                    "  Choose a Pokemon [0-" + pokemonCount + "]: "));

            if (!scanner.hasNextLine()) {
                System.out.println();
                return 0;
            }

            String input = scanner.nextLine().trim();
            try {
                int selection = Integer.parseInt(input);
                if (selection >= 0 && selection <= pokemonCount) {
                    return selection;
                }
            } catch (NumberFormatException exception) {
                // The shared message also handles non-numeric input.
            }

            System.out.println(coral(
                    "  Invalid choice. Enter 0 through " + pokemonCount + "."));
        }
    }

    private void showTeamConfirmation(Pokemon[] selectedTeam) {
        System.out.println();
        printBodyTop();
        printPowerIndicator();
        printScreenBorder();
        printScreenLine("", 0);
        printCenteredScreenLine(boldCream("TEAM READY"), 10);
        printCenteredScreenLine(gray("Team: 3/3"), 9);
        printScreenDivider();
        printScreenLine("", 0);

        for (int index = 0; index < selectedTeam.length; index++) {
            String line = "[" + (index + 1) + "] "
                    + selectedTeam[index].getName() + "  ["
                    + selectedTeam[index].getType() + "]";
            printCenteredScreenLine(white(line), line.length());
        }

        printScreenLine("", 0);
        printCenteredScreenLine(
                cream("Your three Pokemon are confirmed."), 33);
        printCenteredScreenLine(
                gray("Battle mechanics arrive in the next milestone."), 46);
        printScreenLine("", 0);
        printScreenBorder();
        printPhysicalControls();
        printSelectAndStart();
        printCompactSpeaker();
        printBodyBottom();
    }

    private String selectedNames(Pokemon[] selectedTeam, int selectedCount) {
        if (selectedCount == 0) {
            return "None";
        }

        StringBuilder names = new StringBuilder();
        for (int index = 0; index < selectedCount; index++) {
            if (index > 0) {
                names.append(", ");
            }
            names.append(selectedTeam[index].getName());
        }
        return names.toString();
    }

    public void showFarewellMessage() {
        System.out.println();
        System.out.println(gray("  > ")
                + white("Thanks for visiting the arena. See you next time!"));
        System.out.print(reset());
    }

    public void waitForEnter(Scanner scanner) {
        System.out.println();
        System.out.print(cream("  Press ENTER to return to the main menu..."));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
    }

    private void printScreenMenuItem(int number, String label) {
        String styledItem = coral("[" + number + "]") + " " + cream(label);
        int visibleLength = label.length() + 4;
        int leftPadding = (SCREEN_WIDTH - 24) / 2;
        printScreenLine(repeat(' ', leftPadding) + styledItem, leftPadding + visibleLength);
    }

    private void printPowerIndicator() {
        String indicator = "  " + coral("(*)") + gray(" POWER");
        printBodyLine(indicator, 11);
    }

    private void printBodyTop() {
        System.out.println(cherry("+" + repeat('-', BODY_WIDTH) + "+"));
    }

    private void printBodyBottom() {
        System.out.println(cherry("\\" + repeat('_', BODY_WIDTH) + "/"));
    }

    private void printScreenDivider() {
        printScreenLine(berry(repeat('-', SCREEN_WIDTH)), SCREEN_WIDTH);
    }

    private void printScreenBorder() {
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenBorder = margin + berry("+" + repeat('-', SCREEN_WIDTH) + "+") + margin;
        printBodyLine(screenBorder, BODY_WIDTH);
    }

    private void printScreenLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, SCREEN_WIDTH - visibleLength);
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenLine = margin + berry("|") + styledText
                + repeat(' ', rightPadding) + berry("|") + margin;
        printBodyLine(screenLine, BODY_WIDTH);
    }

    private void printCenteredScreenLine(String styledText, int visibleLength) {
        int leftPadding = Math.max(0, (SCREEN_WIDTH - visibleLength) / 2);
        int rightPadding = Math.max(0, SCREEN_WIDTH - visibleLength - leftPadding);
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenLine = margin + berry("|")
                + repeat(' ', leftPadding) + styledText + repeat(' ', rightPadding)
                + berry("|") + margin;
        printBodyLine(screenLine, BODY_WIDTH);
    }

    private void printContentPageLine(String styledText, int visibleLength) {
        String paddedText = "  " + styledText;
        printScreenLine(paddedText, visibleLength + 2);
    }

    private void printPhysicalControls() {
        printControlLine(D_PAD[0], "" + coral("(A)"), 3);
        printControlLine(D_PAD[1], berry("(B)"), 3);
        printControlLine(D_PAD[2], "", 0);
    }

    private void printControlLine(String dPad, String button, int buttonLength) {
        String content = "        " + gray(dPad) + repeat(' ', 27) + button;
        printBodyLine(content, 8 + dPad.length() + 27 + buttonLength);
    }

    private void printSelectAndStart() {
        String controls = warmRose("[ SELECT ]  [ START ]");
        printCenteredBodyLine(controls, 21);
    }

    private void printSpeaker() {
        for (String line : SPEAKER) {
            String content = repeat(' ', 49) + gray(line);
            printBodyLine(content, 49 + line.length());
        }
    }

    private void printCompactSpeaker() {
        String line = SPEAKER[0];
        String content = repeat(' ', 49) + gray(line);
        printBodyLine(content, 49 + line.length());
    }

    private void printCenteredBodyLine(String styledText, int visibleLength) {
        int leftPadding = Math.max(0, (BODY_WIDTH - visibleLength) / 2);
        printBodyLine(repeat(' ', leftPadding) + styledText, leftPadding + visibleLength);
    }

    private void printBodyLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, BODY_WIDTH - visibleLength);
        System.out.println(cherry("|") + styledText
                + repeat(' ', rightPadding) + cherry("|"));
    }

    private String repeat(char character, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            result.append(character);
        }
        return result.toString();
    }

    private String padRight(String text, int width) {
        StringBuilder result = new StringBuilder(text);
        while (result.length() < width) {
            result.append(' ');
        }
        return result.toString();
    }

    private String padLeft(int number, int width) {
        String text = Integer.toString(number);
        StringBuilder result = new StringBuilder();
        while (result.length() + text.length() < width) {
            result.append(' ');
        }
        result.append(text);
        return result.toString();
    }

    // Color helpers return plain text whenever colors are disabled.
    private String coral(String text) {
        return style(text, CORAL_RED);
    }

    private String berry(String text) {
        return style(text, BERRY_RED);
    }

    private String cherry(String text) {
        return style(text, CHERRY_WINE);
    }

    private String cream(String text) {
        return style(text, CREAM_PEACH);
    }

    private String dustyRose(String text) {
        return style(text, DUSTY_ROSE);
    }

    private String warmRose(String text) {
        return style(text, WARM_ROSE);
    }

    private String white(String text) {
        return style(text, SOFT_WHITE);
    }

    private String gray(String text) {
        return style(text, WARM_GRAY);
    }

    private String boldCream(String text) {
        return style(text, BOLD + CREAM_PEACH);
    }

    private String boldWhite(String text) {
        return style(text, BOLD + SOFT_WHITE);
    }

    private String reset() {
        return colorsEnabled ? RESET : "";
    }

    private String style(String text, String codes) {
        if (!colorsEnabled) {
            return text;
        }
        return codes + text + RESET;
    }

    private static String foreground(int red, int green, int blue) {
        return ESCAPE + "38;2;" + red + ";" + green + ";" + blue + "m";
    }
}
