// Displays the menus, team-selection screens, prompts, errors, and other terminal content.

import java.util.Scanner;

public final class TerminalUI {
    // Display dimensions.
    private static final int BODY_WIDTH = 68;
    private static final int SCREEN_WIDTH = 60;
    private static final int SCREEN_MARGIN = 3;
    private static final int BATTLE_BODY_WIDTH = 92;
    private static final int BATTLE_SCREEN_WIDTH = 84;
    private static final int BATTLE_SCREEN_MARGIN = 3;
    private static final int MOVE_CARD_WIDTH = 39;
    private static final int MOVE_CARD_CONTENT_WIDTH = MOVE_CARD_WIDTH - 2;
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

    private static final String[] POKE_BALL = {
        "     .------.     ",
        "   /          \\   ",
        "  |----()------|  ",
        "   \\          /   ",
        "     '------'     "
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
        waitForEnter(scanner, "  Press ENTER to continue...");
        return completedTeam;
    }

    public void runBattle(Scanner scanner, BattleGame battleGame) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner must not be null.");
        }
        if (battleGame == null) {
            throw new IllegalArgumentException("Battle game must not be null.");
        }

        showBattleTeams(battleGame);
        waitForEnter(scanner, "  Press ENTER to begin the battle...");

        while (!battleGame.isBattleOver()) {
            showBattleScreen(battleGame);
            int moveNumber = readBattleMoveNumber(
                    scanner, battleGame.getActivePlayerPokemon());
            if (moveNumber == 0) {
                System.out.println();
                System.out.println(gray("  > Battle left. Returning to the main menu."));
                return;
            }

            battleGame.executeTurn(moveNumber - 1);
            showTurnResult(battleGame);
            if (battleGame.isBattleOver()) {
                waitForEnter(scanner, "  Press ENTER to view the final result...");
            } else {
                waitForEnter(scanner, "  Press ENTER to continue...");
            }
        }

        showBattleResult(battleGame);
        waitForEnter(scanner, "  Press ENTER to return to the Main Menu...");
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
                gray("Your team is ready for battle."), 30);
        printScreenLine("", 0);
        printScreenBorder();
        printPhysicalControls();
        printSelectAndStart();
        printCompactSpeaker();
        printBodyBottom();
    }

    private void showBattleTeams(BattleGame battleGame) {
        System.out.println();
        printBattleBodyTop();
        printBattlePowerIndicator();
        printBattleScreenBorder();
        printBattleScreenLine("", 0);
        printCenteredBattleScreenLine(boldCream("BATTLE TEAMS"), 12);
        printBattleScreenDivider();
        printBattleContentLine(cream("PLAYER TEAM"), 11);
        printBattleTeamList(battleGame.getPlayerTeam());
        printBattleScreenLine("", 0);
        printBattleContentLine(coral("CPU TEAM"), 8);
        printBattleTeamList(battleGame.getCpuTeam());
        printBattleScreenLine("", 0);
        printBattleScreenBorder();
        printBattleControls();
        printBattleBodyBottom();
    }

    private void printBattleTeamList(Pokemon[] team) {
        for (int index = 0; index < team.length; index++) {
            String line = "[" + (index + 1) + "] " + team[index].getName()
                    + "  [" + team[index].getType() + "]  HP "
                    + team[index].getCurrentHp() + "/" + team[index].getMaxHp();
            printBattleContentLine(white(line), line.length());
        }
    }

    private void showBattleScreen(BattleGame battleGame) {
        Pokemon playerPokemon = battleGame.getActivePlayerPokemon();
        Pokemon cpuPokemon = battleGame.getActiveCpuPokemon();
        String title = "BATTLE - TURN " + battleGame.getTurnNumber();

        System.out.println();
        printBattleBodyTop();
        printBattlePowerIndicator();
        printBattleScreenBorder();
        printBattleScreenLine("", 0);
        printCenteredBattleScreenLine(boldCream(title), title.length());
        printBattleScreenDivider();
        printBattlefield(battleGame, cpuPokemon, playerPokemon);
        printBattleScreenDivider();
        printBattleContentLine(boldWhite("CHOOSE A MOVE"), 13);
        printMoveCards(playerPokemon.getMoves());
        printBattleContentLine(gray("[0] Leave Battle"), 16);
        printBattleScreenLine("", 0);
        printBattleScreenBorder();
        printBattleControls();
        printBattleBodyBottom();
    }

    private void printBattlefield(BattleGame battleGame,
            Pokemon cpuPokemon, Pokemon playerPokemon) {
        String[] cpuStatus = createStatusPanel(
                "CPU ACTIVE", cpuPokemon, battleGame.getCpuTeam());
        String[] playerStatus = createStatusPanel(
                "PLAYER ACTIVE", playerPokemon, battleGame.getPlayerTeam());

        for (int index = 0; index < POKE_BALL.length; index++) {
            printBattlefieldColumns(cpuStatus[index], POKE_BALL[index], true);
        }
        printCenteredBattleScreenLine(gray("- VS -"), 6);
        for (int index = 0; index < POKE_BALL.length; index++) {
            printBattlefieldColumns(POKE_BALL[index], playerStatus[index], false);
        }
    }

    private String[] createStatusPanel(
            String heading, Pokemon pokemon, Pokemon[] team) {
        return new String[] {
            heading,
            pokemon.getName() + "  [" + pokemon.getType() + "]",
            "HP " + createHpBar(pokemon) + "  "
                    + pokemon.getCurrentHp() + "/" + pokemon.getMaxHp(),
            "Team able: " + countAblePokemon(team) + "/" + team.length,
            ""
        };
    }

    private int countAblePokemon(Pokemon[] team) {
        int count = 0;
        for (Pokemon pokemon : team) {
            if (!pokemon.isFainted()) {
                count++;
            }
        }
        return count;
    }

    private void printBattlefieldColumns(
            String left, String right, boolean cpuRow) {
        String leftText = centerPlainText(left, 40);
        String rightText = centerPlainText(right, 40);
        String styledLeft = cpuRow ? coral(leftText) : gray(leftText);
        String styledRight = cpuRow ? gray(rightText) : cream(rightText);
        printBattleScreenLine(styledLeft + "    " + styledRight,
                BATTLE_SCREEN_WIDTH);
    }

    private void printMoveCards(Move[] moves) {
        for (int row = 0; row < moves.length; row += 2) {
            Move leftMove = moves[row];
            Move rightMove = row + 1 < moves.length ? moves[row + 1] : null;

            printMoveCardPair(moveCardBorder(),
                    rightMove == null ? "" : moveCardBorder());
            printMoveCardPair(createMoveNameLine(leftMove, row + 1),
                    rightMove == null ? ""
                            : createMoveNameLine(rightMove, row + 2));
            printMoveCardPair(createMoveDetailLine(leftMove),
                    rightMove == null ? ""
                            : createMoveDetailLine(rightMove));
            printMoveCardPair(moveCardBorder(),
                    rightMove == null ? "" : moveCardBorder());
            if (row + 2 < moves.length) {
                printBattleScreenLine("", 0);
            }
        }
    }

    private String moveCardBorder() {
        return berry("+" + repeat('-', MOVE_CARD_WIDTH - 2) + "+");
    }

    private String createMoveNameLine(Move move, int number) {
        String prefix = " [" + number + "] ";
        String name = fitText(move.getName().toUpperCase(),
                MOVE_CARD_CONTENT_WIDTH - prefix.length());
        int remainingSpace = MOVE_CARD_CONTENT_WIDTH
                - prefix.length() - name.length();
        String styledContent = " " + coral("[" + number + "]")
                + white(" " + name)
                + repeat(' ', remainingSpace);
        return berry("|") + styledContent + berry("|");
    }

    private String createMoveDetailLine(Move move) {
        String details = String.format(" %-8s PP %2d/%-2d PWR %3d PRI %d",
                move.getType(), move.getCurrentPp(), move.getMaxPp(),
                move.getPower(), move.getPriority());
        details = fitText(details, MOVE_CARD_CONTENT_WIDTH);
        details = padRight(details, MOVE_CARD_CONTENT_WIDTH);
        String styledDetails = move.hasPp() ? white(details) : cherry(details);
        return berry("|") + styledDetails + berry("|");
    }

    private void printMoveCardPair(String leftCard, String rightCard) {
        String left = leftCard.isEmpty()
                ? repeat(' ', MOVE_CARD_WIDTH) : leftCard;
        String right = rightCard.isEmpty()
                ? repeat(' ', MOVE_CARD_WIDTH) : rightCard;
        String cards = "  " + left + "  " + right + "  ";
        printBattleScreenLine(cards, BATTLE_SCREEN_WIDTH);
    }

    private String createHpBar(Pokemon pokemon) {
        int filledLength = (int) Math.round(
                pokemon.getCurrentHp() * 10.0 / pokemon.getMaxHp());
        StringBuilder bar = new StringBuilder("[");
        for (int index = 0; index < 10; index++) {
            bar.append(index < filledLength ? '#' : '-');
        }
        bar.append(']');
        return bar.toString();
    }

    private int readBattleMoveNumber(Scanner scanner, Pokemon playerPokemon) {
        int moveCount = playerPokemon.getMoves().length;
        while (true) {
            System.out.print(cream(
                    "  Choose a move [0-" + moveCount + "]: "));

            if (!scanner.hasNextLine()) {
                System.out.println();
                return 0;
            }

            String input = scanner.nextLine().trim();
            try {
                int moveNumber = Integer.parseInt(input);
                if (moveNumber == 0) {
                    return 0;
                }
                if (moveNumber >= 1 && moveNumber <= moveCount) {
                    Move selectedMove = playerPokemon.getMove(moveNumber - 1);
                    if (!selectedMove.hasPp()) {
                        System.out.println(coral("  " + selectedMove.getName()
                                + " has no PP remaining. Choose another move."));
                    } else {
                        return moveNumber;
                    }
                } else {
                    System.out.println(coral("  Invalid move. Enter 0 through "
                            + moveCount + "."));
                }
            } catch (NumberFormatException exception) {
                System.out.println(coral(
                        "  Invalid move. Please enter a number."));
            }
        }
    }

    private void showTurnResult(BattleGame battleGame) {
        String title = "TURN " + (battleGame.getTurnNumber() - 1) + " RESULT";
        System.out.println();
        printBattleBodyTop();
        printBattlePowerIndicator();
        printBattleScreenBorder();
        printBattleScreenLine("", 0);
        printCenteredBattleScreenLine(boldCream(title), title.length());
        printBattleScreenDivider();
        printBattleContentLine(coral("TURN ORDER FROM MAX-HEAP"), 24);

        String[] turnOrder = battleGame.getLastTurnOrder();
        for (int index = 0; index < turnOrder.length; index++) {
            String line = (index + 1) + ". " + turnOrder[index];
            printWrappedBattleContent(line, true);
        }

        printBattleScreenLine("", 0);
        printBattleContentLine(cream("BATTLE RESULT"), 13);
        String[] battleLog = battleGame.getLastTurnLog();
        for (String line : battleLog) {
            printPlayerFacingLogLine(line);
        }

        printBattleScreenLine("", 0);
        printBattleContentLine(gray("UPDATED STATUS"), 14);
        printBattleCompactHp("CPU", battleGame.getActiveCpuPokemon());
        printBattleCompactHp("YOU", battleGame.getActivePlayerPokemon());
        printReplacementMessages(battleLog);
        printBattleScreenLine("", 0);
        printBattleScreenBorder();
        printBattleControls();
        printBattleBodyBottom();
    }

    private void printPlayerFacingLogLine(String line) {
        if (isAdministrativeLogLine(line) || isReplacementLogLine(line)) {
            return;
        }

        int usedIndex = line.indexOf(" used ");
        int damageIndex = line.indexOf(" for ", usedIndex + 1);
        int receivedIndex = line.indexOf(" damage. ", damageIndex + 1);
        int hpIndex = line.indexOf(" HP: ", receivedIndex + 1);
        if (usedIndex >= 0 && damageIndex >= 0
                && receivedIndex >= 0 && hpIndex >= 0) {
            String action = line.substring(0, damageIndex) + "!";
            String damage = line.substring(damageIndex + 5, receivedIndex);
            String target = line.substring(receivedIndex + 9, hpIndex);
            printWrappedBattleContent(action, false);
            printWrappedBattleContent(
                    target + " received " + damage + " damage.", false);
            return;
        }

        String playerMessage = line;
        if (line.endsWith(", but it missed.")) {
            playerMessage = line.substring(0, line.length() - 16)
                    + " - attack missed!";
        } else if (line.equals("It's super effective.")) {
            playerMessage = "It's super effective!";
        } else if (line.equals("It's not very effective.")) {
            playerMessage = "It's not very effective.";
        }
        printWrappedBattleContent(playerMessage, false);
    }

    private boolean isAdministrativeLogLine(String line) {
        return line.startsWith("Turn ")
                || line.startsWith("Player chose ")
                || line.startsWith("CPU chose ")
                || line.startsWith("Heap extracted: ");
    }

    private boolean isReplacementLogLine(String line) {
        return line.startsWith("Player sends out ")
                || line.startsWith("CPU sends out ");
    }

    private void printReplacementMessages(String[] battleLog) {
        for (String line : battleLog) {
            if (isReplacementLogLine(line)) {
                String lineWithoutPeriod = line.endsWith(".")
                        ? line.substring(0, line.length() - 1) : line;
                String message = lineWithoutPeriod.replace(" sends out ",
                        " replacement: ") + " entered.";
                printWrappedBattleContent(message, true);
            }
        }
    }

    private void printWrappedBattleContent(String text, boolean highlighted) {
        int maximumLength = BATTLE_SCREEN_WIDTH - 4;
        String remaining = text;

        while (remaining.length() > maximumLength) {
            int splitIndex = remaining.lastIndexOf(' ', maximumLength);
            if (splitIndex <= 0) {
                splitIndex = maximumLength;
            }
            String line = remaining.substring(0, splitIndex);
            printBattleContentLine(
                    highlighted ? cream(line) : white(line), line.length());
            remaining = remaining.substring(splitIndex).trim();
        }

        printBattleContentLine(
                highlighted ? cream(remaining) : white(remaining),
                remaining.length());
    }

    private void printBattleCompactHp(String side, Pokemon pokemon) {
        String line = side + ": " + pokemon.getName() + "  HP "
                + pokemon.getCurrentHp() + "/" + pokemon.getMaxHp();
        printBattleContentLine(white(line), line.length());
    }

    private void showBattleResult(BattleGame battleGame) {
        String result = battleGame.didPlayerWin() ? "VICTORY" : "DEFEAT";
        String message = battleGame.didPlayerWin()
                ? "All CPU Pokemon have fainted."
                : "All player Pokemon have fainted.";

        System.out.println();
        printBattleBodyTop();
        printBattlePowerIndicator();
        printBattleScreenBorder();
        printBattleScreenLine("", 0);
        printCenteredBattleScreenLine(boldCream("BATTLE COMPLETE"), 15);
        printBattleScreenDivider();
        printBattleScreenLine("", 0);
        String resultWithMark = result + "!";
        printCenteredBattleScreenLine(
                battleGame.didPlayerWin()
                        ? boldWhite(resultWithMark) : coral(resultWithMark),
                resultWithMark.length());
        printCenteredBattleScreenLine(white(message), message.length());
        printBattleScreenLine("", 0);
        printBattleContentLine(cream("PLAYER TEAM"), 11);
        printFinalBattleTeamHp(battleGame.getPlayerTeam());
        printBattleContentLine(coral("CPU TEAM"), 8);
        printFinalBattleTeamHp(battleGame.getCpuTeam());
        printBattleScreenLine("", 0);
        printBattleScreenBorder();
        printBattleControls();
        printBattleBodyBottom();
    }

    private void printFinalBattleTeamHp(Pokemon[] team) {
        for (Pokemon pokemon : team) {
            String line = pokemon.getName() + "  HP "
                    + pokemon.getCurrentHp() + "/" + pokemon.getMaxHp();
            printBattleContentLine(white(line), line.length());
        }
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
        waitForEnter(scanner, "  Press ENTER to return to the main menu...");
    }

    private void waitForEnter(Scanner scanner, String prompt) {
        System.out.println();
        System.out.print(cream(prompt));
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

    private void printBattleBodyTop() {
        System.out.println(cherry("+" + repeat('-', BATTLE_BODY_WIDTH) + "+"));
    }

    private void printBattleBodyBottom() {
        System.out.println(cherry("\\" + repeat('_', BATTLE_BODY_WIDTH) + "/"));
    }

    private void printBattlePowerIndicator() {
        String indicator = "  " + coral("(*)") + gray(" POWER");
        printBattleBodyLine(indicator, 11);
    }

    private void printBattleScreenBorder() {
        String margin = repeat(' ', BATTLE_SCREEN_MARGIN);
        String border = margin + berry("+"
                + repeat('-', BATTLE_SCREEN_WIDTH) + "+") + margin;
        printBattleBodyLine(border, BATTLE_BODY_WIDTH);
    }

    private void printBattleScreenDivider() {
        printBattleScreenLine(
                berry(repeat('-', BATTLE_SCREEN_WIDTH)), BATTLE_SCREEN_WIDTH);
    }

    private void printBattleScreenLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, BATTLE_SCREEN_WIDTH - visibleLength);
        String margin = repeat(' ', BATTLE_SCREEN_MARGIN);
        String line = margin + berry("|") + styledText
                + repeat(' ', rightPadding) + berry("|") + margin;
        printBattleBodyLine(line, BATTLE_BODY_WIDTH);
    }

    private void printCenteredBattleScreenLine(
            String styledText, int visibleLength) {
        int leftPadding = Math.max(
                0, (BATTLE_SCREEN_WIDTH - visibleLength) / 2);
        int rightPadding = Math.max(
                0, BATTLE_SCREEN_WIDTH - visibleLength - leftPadding);
        String margin = repeat(' ', BATTLE_SCREEN_MARGIN);
        String line = margin + berry("|") + repeat(' ', leftPadding)
                + styledText + repeat(' ', rightPadding)
                + berry("|") + margin;
        printBattleBodyLine(line, BATTLE_BODY_WIDTH);
    }

    private void printBattleContentLine(String styledText, int visibleLength) {
        printBattleScreenLine("  " + styledText, visibleLength + 2);
    }

    private void printBattleControls() {
        printBattleControlLine(D_PAD[0], coral("(A)"), 3);
        printBattleControlLine(D_PAD[1], berry("(B)"), 3);
        printBattleControlLine(D_PAD[2], "", 0);

        String selectAndStart = warmRose("[ SELECT ]  [ START ]");
        int controlPadding = (BATTLE_BODY_WIDTH - 21) / 2;
        printBattleBodyLine(
                repeat(' ', controlPadding) + selectAndStart,
                controlPadding + 21);

        String speaker = SPEAKER[0];
        String speakerLine = repeat(' ', BATTLE_BODY_WIDTH - speaker.length() - 8)
                + gray(speaker);
        printBattleBodyLine(speakerLine, BATTLE_BODY_WIDTH - 8);
    }

    private void printBattleControlLine(
            String dPad, String button, int buttonLength) {
        int spacing = BATTLE_BODY_WIDTH - 16 - dPad.length() - buttonLength;
        String content = "        " + gray(dPad)
                + repeat(' ', spacing) + button + repeat(' ', 8);
        printBattleBodyLine(content, BATTLE_BODY_WIDTH);
    }

    private void printBattleBodyLine(String styledText, int visibleLength) {
        int rightPadding = Math.max(0, BATTLE_BODY_WIDTH - visibleLength);
        System.out.println(cherry("|") + styledText
                + repeat(' ', rightPadding) + cherry("|"));
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

    private String centerPlainText(String text, int width) {
        String fittedText = fitText(text, width);
        int leftPadding = (width - fittedText.length()) / 2;
        int rightPadding = width - fittedText.length() - leftPadding;
        return repeat(' ', leftPadding) + fittedText
                + repeat(' ', rightPadding);
    }

    private String fitText(String text, int width) {
        if (text.length() <= width) {
            return text;
        }
        if (width <= 3) {
            return text.substring(0, width);
        }
        return text.substring(0, width - 3) + "...";
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
