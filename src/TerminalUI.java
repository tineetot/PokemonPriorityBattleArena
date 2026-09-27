import java.util.Scanner;

public final class TerminalUI {
    private static final int BODY_WIDTH = 68;
    private static final int SCREEN_WIDTH = 60;
    private static final int SCREEN_MARGIN = 3;
    private static final int CONTENT_PAGE_WIDTH = 56;
    private static final int CONTENT_PAGE_HEIGHT = 16;
    private static final int MINIMUM_MENU_CHOICE = 1;
    private static final int MAXIMUM_MENU_CHOICE = 5;

    private final AnsiTheme theme;

    public TerminalUI(AnsiTheme theme) {
        this.theme = theme;
    }

    public void showTitleScreen() {
        printBodyTop();
        printBodyLine("", 0);
        printPowerIndicator();
        printScreenBorder();
        printScreenLine("", 0);
        for (String line : AsciiArt.pokemonLogo()) {
            printCenteredScreenLine(theme.coral(line), line.length());
        }
        printCenteredScreenLine(theme.boldCream(GameConfig.GAME_TITLE), GameConfig.GAME_TITLE.length());
        printScreenLine("", 0);
        for (String line : AsciiArt.battleScene()) {
            printCenteredScreenLine(theme.gray(line), line.length());
        }
    }

    public void showMainMenu() {
        printScreenDivider();
        printCenteredScreenLine(theme.boldWhite("MAIN MENU"), 9);
        printScreenMenuItem(1, "Start Battle");
        printScreenMenuItem(2, "How to Play");
        printScreenMenuItem(3, "Heap Demonstration");
        printScreenMenuItem(4, "Credits");
        printScreenMenuItem(5, "Exit");
        printScreenLine("", 0);
        printScreenBorder();
        printBodyLine("", 0);
        printPhysicalControls();
        printSelectAndStart();
        printSpeaker();
        printBodyLine("", 0);
        printBodyBottom();
    }

    public void renderGameBoyPage(String title, String pageIndicator,
            String[] contentLines, String navigationHint) {
        if (title == null || pageIndicator == null
                || contentLines == null || navigationHint == null) {
            throw new IllegalArgumentException(
                    "Page title, indicator, content, and navigation hint are required.");
        }
        if (title.length() > CONTENT_PAGE_WIDTH
                || pageIndicator.length() > CONTENT_PAGE_WIDTH
                || navigationHint.length() > CONTENT_PAGE_WIDTH) {
            throw new IllegalArgumentException("Page title and navigation hint must fit the screen.");
        }
        if (contentLines.length > CONTENT_PAGE_HEIGHT) {
            throw new IllegalArgumentException("A content page supports at most "
                    + CONTENT_PAGE_HEIGHT + " content lines.");
        }

        System.out.println();
        printBodyTop();
        printPowerIndicator();
        printScreenBorder();
        printScreenLine("", 0);
        printCenteredScreenLine(theme.boldCream(title), title.length());
        printCenteredScreenLine(theme.gray(pageIndicator), pageIndicator.length());
        printScreenDivider();

        int unusedLines = CONTENT_PAGE_HEIGHT - contentLines.length;
        int topPadding = unusedLines / 2;
        int bottomPadding = unusedLines - topPadding;
        for (int index = 0; index < topPadding; index++) {
            printContentPageLine("", 0);
        }
        for (String line : contentLines) {
            if (line == null || line.length() > CONTENT_PAGE_WIDTH) {
                throw new IllegalArgumentException(
                        "Every page content line must be non-null and at most "
                        + CONTENT_PAGE_WIDTH + " characters.");
            }
            printContentPageLine(theme.white(line), line.length());
        }
        for (int index = 0; index < bottomPadding; index++) {
            printContentPageLine("", 0);
        }

        printScreenLine("", 0);
        printCenteredScreenLine(theme.cream(navigationHint), navigationHint.length());
        printScreenBorder();
        printPhysicalControls();
        printSelectAndStart();
        printCompactSpeaker();
        printBodyBottom();
        System.out.print(theme.cream("  Command [Enter/A/B/M]: "));
    }

    public String readPageCommand(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.isEmpty()
                    || command.equalsIgnoreCase("a")
                    || command.equalsIgnoreCase("b")
                    || command.equalsIgnoreCase("m")) {
                System.out.println();
                return command;
            }

            System.out.println(theme.coral("  Invalid command. Use Enter, A, B, or M."));
            System.out.print(theme.cream("  Command [Enter/A/B/M]: "));
        }

        System.out.println();
        return "m";
    }

    public int readMenuSelection(Scanner scanner) {
        while (true) {
            System.out.print(theme.cream("  Select an option [1-5]: "));

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

            System.out.println(theme.coral("  Invalid choice. Please enter a number from 1 to 5."));
        }
    }

    public void showSelectionMessage(int selection) {
        String message;

        switch (selection) {
            case 1:
                message = "Battle setup will arrive in a future milestone.";
                break;
            case 2:
                message = "How-to-play instructions will be added with the battle rules.";
                break;
            case 3:
                message = "The custom max-heap demonstration is planned for a later milestone.";
                break;
            case 4:
                message = "Created by the Pokemon Priority Battle Arena development team.";
                break;
            case 5:
                message = "Thanks for visiting the arena. See you next time!";
                break;
            default:
                message = "Unknown menu selection.";
                break;
        }

        System.out.println();
        System.out.println(theme.gray("  > ") + theme.white(message));
        System.out.print(theme.reset());
    }

    public void waitForEnter(Scanner scanner) {
        System.out.println();
        System.out.print(theme.cream("  Press ENTER to return to the main menu..."));
        if (scanner.hasNextLine()) {
            scanner.nextLine();
        }
        System.out.println();
    }

    private void printScreenMenuItem(int number, String label) {
        String styledItem = theme.coral("[" + number + "]") + " " + theme.cream(label);
        int visibleLength = label.length() + 4;
        int leftPadding = (SCREEN_WIDTH - 24) / 2;
        printScreenLine(repeat(' ', leftPadding) + styledItem, leftPadding + visibleLength);
    }

    private void printPowerIndicator() {
        String indicator = "  " + theme.coral("(*)") + theme.gray(" POWER");
        printBodyLine(indicator, 11);
    }

    private void printBodyTop() {
        System.out.println(theme.cherry("+" + repeat('-', BODY_WIDTH) + "+"));
    }

    private void printBodyBottom() {
        System.out.println(theme.cherry("\\" + repeat('_', BODY_WIDTH) + "/"));
    }

    private void printScreenDivider() {
        printScreenLine(theme.berry(repeat('-', SCREEN_WIDTH)), SCREEN_WIDTH);
    }

    private void printScreenBorder() {
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenBorder = margin + theme.berry("+" + repeat('-', SCREEN_WIDTH) + "+") + margin;
        printBodyLine(screenBorder, BODY_WIDTH);
    }

    private void printScreenLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, SCREEN_WIDTH - visibleLength);
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenLine = margin + theme.berry("|") + styledText
                + repeat(' ', rightPadding) + theme.berry("|") + margin;
        printBodyLine(screenLine, BODY_WIDTH);
    }

    private void printCenteredScreenLine(String styledText, int visibleLength) {
        int leftPadding = Math.max(0, (SCREEN_WIDTH - visibleLength) / 2);
        int rightPadding = Math.max(0, SCREEN_WIDTH - visibleLength - leftPadding);
        String margin = repeat(' ', SCREEN_MARGIN);
        String screenLine = margin + theme.berry("|")
                + repeat(' ', leftPadding) + styledText + repeat(' ', rightPadding)
                + theme.berry("|") + margin;
        printBodyLine(screenLine, BODY_WIDTH);
    }

    private void printContentPageLine(String styledText, int visibleLength) {
        String paddedText = "  " + styledText;
        printScreenLine(paddedText, visibleLength + 2);
    }

    private void printPhysicalControls() {
        String[] dPad = AsciiArt.dPad();
        printControlLine(dPad[0], "" + theme.coral("(A)"), 3);
        printControlLine(dPad[1], theme.berry("(B)"), 3);
        printControlLine(dPad[2], "", 0);
    }

    private void printControlLine(String dPad, String button, int buttonLength) {
        String content = "        " + theme.gray(dPad) + repeat(' ', 27) + button;
        printBodyLine(content, 8 + dPad.length() + 27 + buttonLength);
    }

    private void printSelectAndStart() {
        String controls = theme.warmRose("[ SELECT ]  [ START ]");
        printCenteredBodyLine(controls, 21);
    }

    private void printSpeaker() {
        for (String line : AsciiArt.speaker()) {
            String content = repeat(' ', 49) + theme.gray(line);
            printBodyLine(content, 49 + line.length());
        }
    }

    private void printCompactSpeaker() {
        String line = AsciiArt.speaker()[0];
        String content = repeat(' ', 49) + theme.gray(line);
        printBodyLine(content, 49 + line.length());
    }

    private void printCenteredBodyLine(String styledText, int visibleLength) {
        int leftPadding = Math.max(0, (BODY_WIDTH - visibleLength) / 2);
        printBodyLine(repeat(' ', leftPadding) + styledText, leftPadding + visibleLength);
    }

    private void printBodyLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, BODY_WIDTH - visibleLength);
        System.out.println(theme.cherry("|") + styledText
                + repeat(' ', rightPadding) + theme.cherry("|"));
    }

    private String repeat(char character, int count) {
        StringBuilder result = new StringBuilder(count);
        for (int index = 0; index < count; index++) {
            result.append(character);
        }
        return result.toString();
    }
}
