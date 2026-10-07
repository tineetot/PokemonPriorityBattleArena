public final class BattleGameTest {
    private static int checksPassed = 0;

    private BattleGameTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        testTeamsAndDefensiveCopies();
        testPriorityAndDamage();
        testSpeedOrder();
        testFaintingSkipAndReplacement();
        testCpuMoveChoice();
        testCpuWithoutPp();
        testPlayerVictory();
        testCpuVictory();
        testInvalidMovesAndTeams();

        System.out.println("All " + checksPassed + " BattleGame checks passed.");
    }

    private static void testTeamsAndDefensiveCopies() {
        Pokemon[] available = createStandardRoster(0, 0, 60, 50);
        Pokemon[] selected = {available[1], available[0], available[2]};
        BattleGame game = new BattleGame(available, selected);

        Pokemon[] cpuTeam = game.getCpuTeam();
        check(cpuTeam.length == 3, "The CPU team should contain exactly three Pokemon.");
        check(cpuTeam[0] == available[3]
                        && cpuTeam[1] == available[4]
                        && cpuTeam[2] == available[5],
                "The CPU should take the first three unselected Pokemon.");
        check(!containsAnyReference(cpuTeam, selected),
                "The CPU team must exclude every selected player Pokemon.");
        check(game.getActivePlayerPokemon() == selected[0],
                "The first selected player Pokemon should start active.");
        check(game.getActiveCpuPokemon() == cpuTeam[0],
                "The first CPU Pokemon should start active.");
        check(game.getTurnNumber() == 1, "A new battle should start on turn one.");

        Pokemon originalPlayer = game.getPlayerTeam()[0];
        Pokemon[] playerCopy = game.getPlayerTeam();
        playerCopy[0] = available[5];
        check(game.getPlayerTeam()[0] == originalPlayer,
                "The player-team getter should return a defensive copy.");

        Pokemon originalCpu = game.getCpuTeam()[0];
        cpuTeam[0] = available[0];
        check(game.getCpuTeam()[0] == originalCpu,
                "The CPU-team getter should return a defensive copy.");
    }

    private static void testPriorityAndDamage() {
        Pokemon[] available = createStandardRoster(2, 0, 40, 90);
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});
        Move playerMove = game.getActivePlayerPokemon().getMove(0);
        Move cpuMove = game.getActiveCpuPokemon().getMove(0);
        int playerPpBefore = playerMove.getCurrentPp();
        int cpuPpBefore = cpuMove.getCurrentPp();
        int playerHpBefore = game.getActivePlayerPokemon().getCurrentHp();
        int cpuHpBefore = game.getActiveCpuPokemon().getCurrentHp();

        game.executeTurn(0);
        String[] order = game.getLastTurnOrder();
        check(order.length == 2, "A normal turn should extract two actions.");
        check(order[0].startsWith("Player One -"),
                "Higher move priority should execute first.");
        check(playerMove.getCurrentPp() == playerPpBefore - 1,
                "The executed player move should consume one PP.");
        check(cpuMove.getCurrentPp() == cpuPpBefore - 1,
                "The executed CPU move should consume one PP.");
        check(game.getActivePlayerPokemon().getCurrentHp() < playerHpBefore,
                "A successful CPU move should reduce player HP.");
        check(game.getActiveCpuPokemon().getCurrentHp() < cpuHpBefore,
                "A successful player move should reduce CPU HP.");
        check(game.getTurnNumber() == 2,
                "The turn number should increase after processing.");
        check(game.isActionHeapEmpty(), "The custom heap should be empty after a turn.");

        String[] logCopy = game.getLastTurnLog();
        String originalLine = game.getLastTurnLog()[0];
        logCopy[0] = "Changed";
        check(game.getLastTurnLog()[0].equals(originalLine),
                "The log getter should return a defensive copy.");
        String originalOrder = game.getLastTurnOrder()[0];
        order[0] = "Changed";
        check(game.getLastTurnOrder()[0].equals(originalOrder),
                "The turn-order getter should return a defensive copy.");
    }

    private static void testSpeedOrder() {
        Pokemon[] available = createStandardRoster(0, 0, 90, 40);
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});

        game.executeTurn(0);
        check(game.getLastTurnOrder()[0].startsWith("Player One -"),
                "Higher Speed should execute first when move priorities tie.");
    }

    private static void testFaintingSkipAndReplacement() {
        Pokemon[] available = createKnockoutRoster(true);
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});
        Pokemon firstCpu = game.getActiveCpuPokemon();
        Move cpuMove = firstCpu.getMove(0);
        int cpuPpBefore = cpuMove.getCurrentPp();

        game.executeTurn(0);
        check(firstCpu.isFainted(), "The first CPU Pokemon should faint.");
        check(cpuMove.getCurrentPp() == cpuPpBefore,
                "A fainted Pokemon's skipped action must not consume PP.");
        check(game.getActiveCpuPokemon() == available[4],
                "The next CPU Pokemon should become active automatically.");
        check(containsLog(game.getLastTurnLog(), "fainted before acting"),
                "The log should explain why the pending action was skipped.");
        check(containsLog(game.getLastTurnLog(), "CPU sends out CPU Two"),
                "The log should name the automatic CPU replacement.");
    }

    private static void testCpuWithoutPp() {
        Pokemon[] available = createStandardRoster(0, 0, 60, 50);
        Move cpuMove = available[3].getMove(0);
        while (cpuMove.usePp()) {
            // Exhaust the CPU's only move before the battle starts.
        }
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});

        game.executeTurn(0);
        check(game.getLastTurnOrder().length == 1,
                "A CPU Pokemon without PP should not create an action.");
        check(containsLog(game.getLastTurnLog(), "no move with PP remaining"),
                "The log should explain when the CPU cannot choose a move.");
    }

    private static void testCpuMoveChoice() {
        Pokemon[] available = createStandardRoster(0, 0, 60, 50);
        Move exhaustedMove = new Move(
                "Exhausted Move", PokemonType.NORMAL, 40, 100, 0, 1);
        exhaustedMove.usePp();
        Move usableMove = new Move(
                "Usable Move", PokemonType.NORMAL, 40, 100, 0, 5);
        available[3] = new Pokemon("CPU One", PokemonType.NORMAL,
                50, 50, 50, 50, new Move[] {exhaustedMove, usableMove});
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});
        int usablePpBefore = usableMove.getCurrentPp();

        game.executeTurn(0);
        check(exhaustedMove.getCurrentPp() == 0,
                "The CPU should skip its first move when that move has zero PP.");
        check(usableMove.getCurrentPp() == usablePpBefore - 1,
                "The CPU should use its first move that still has PP.");
        check(containsLog(game.getLastTurnOrder(), "CPU One - Usable Move"),
                "The extraction order should contain the CPU's usable move.");
    }

    private static void testPlayerVictory() {
        Pokemon[] available = createKnockoutRoster(true);
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});

        game.executeTurn(0);
        game.executeTurn(0);
        game.executeTurn(0);
        check(game.isBattleOver(), "The battle should end when all CPU Pokemon faint.");
        check(game.didPlayerWin(), "Defeating all CPU Pokemon should produce a player win.");
        check(!game.didCpuWin(), "A player victory should not count as a CPU victory.");
        check(containsLog(game.getLastTurnLog(), "Player wins the battle"),
                "The final log should include the player victory message.");
        checkTurnAfterBattleRejected(game);
    }

    private static void testCpuVictory() {
        Pokemon[] available = createKnockoutRoster(false);
        BattleGame game = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});
        int firstPlayerPp = available[0].getMove(0).getCurrentPp();

        game.executeTurn(0);
        check(available[0].getMove(0).getCurrentPp() == firstPlayerPp,
                "A player Pokemon knocked out before acting should not consume PP.");
        game.executeTurn(0);
        game.executeTurn(0);
        check(game.isBattleOver(), "The battle should end when all player Pokemon faint.");
        check(game.didCpuWin(), "Defeating all player Pokemon should produce a CPU win.");
        check(!game.didPlayerWin(), "A CPU victory should not count as a player victory.");
        check(containsLog(game.getLastTurnLog(), "CPU wins the battle"),
                "The final log should include the CPU victory message.");
    }

    private static void testInvalidMovesAndTeams() {
        Pokemon[] available = createStandardRoster(0, 0, 60, 50);
        BattleGame invalidMoveGame = new BattleGame(available,
                new Pokemon[] {available[0], available[1], available[2]});
        checkInvalidIndex(invalidMoveGame, -1,
                "A negative player move index should be rejected.");
        checkInvalidIndex(invalidMoveGame, 1,
                "An unavailable player move index should be rejected.");

        Pokemon[] zeroPpAvailable = createStandardRoster(0, 0, 60, 50);
        Move emptyMove = zeroPpAvailable[0].getMove(0);
        while (emptyMove.usePp()) {
            // Exhaust the move before attempting a turn.
        }
        BattleGame zeroPpGame = new BattleGame(zeroPpAvailable,
                new Pokemon[] {zeroPpAvailable[0], zeroPpAvailable[1], zeroPpAvailable[2]});
        checkTurnRejected(zeroPpGame, 0,
                "A selected player move with zero PP should be rejected.");

        Pokemon[] faintedAvailable = createStandardRoster(0, 0, 60, 50);
        faintedAvailable[0].takeDamage(faintedAvailable[0].getMaxHp());
        BattleGame faintedPlayerGame = new BattleGame(faintedAvailable,
                new Pokemon[] {faintedAvailable[0], faintedAvailable[1], faintedAvailable[2]});
        checkTurnRejected(faintedPlayerGame, 0,
                "A fainted active player Pokemon should not be allowed to act.");

        checkConstructorRejected(available,
                new Pokemon[] {available[0], available[1]},
                "A player team with fewer than three Pokemon should be rejected.");
        checkConstructorRejected(available,
                new Pokemon[] {available[0], available[0], available[1]},
                "Duplicate player Pokemon should be rejected.");
        Pokemon outsider = createPokemon("Outsider", PokemonType.NORMAL,
                50, 0, 40, 100, 10);
        checkConstructorRejected(available,
                new Pokemon[] {available[0], available[1], outsider},
                "Player Pokemon outside the available array should be rejected.");
    }

    private static Pokemon[] createStandardRoster(int playerPriority, int cpuPriority,
            int playerSpeed, int cpuSpeed) {
        return new Pokemon[] {
            createPokemon("Player One", PokemonType.NORMAL,
                    playerSpeed, playerPriority, 40, 100, 10),
            createPokemon("Player Two", PokemonType.FIRE, 55, 0, 40, 100, 10),
            createPokemon("Player Three", PokemonType.WATER, 50, 0, 40, 100, 10),
            createPokemon("CPU One", PokemonType.NORMAL,
                    cpuSpeed, cpuPriority, 40, 100, 10),
            createPokemon("CPU Two", PokemonType.GRASS, 45, 0, 40, 100, 10),
            createPokemon("CPU Three", PokemonType.ELECTRIC, 40, 0, 40, 100, 10)
        };
    }

    private static Pokemon[] createKnockoutRoster(boolean playerActsFirst) {
        int playerPriority = playerActsFirst ? 2 : 0;
        int cpuPriority = playerActsFirst ? 0 : 2;
        int playerPower = playerActsFirst ? 200 : 10;
        int cpuPower = playerActsFirst ? 10 : 200;

        return new Pokemon[] {
            createPokemon("Player One", PokemonType.NORMAL,
                    60, playerPriority, playerPower, 100, 5),
            createPokemon("Player Two", PokemonType.NORMAL,
                    60, 0, 10, 100, 5),
            createPokemon("Player Three", PokemonType.NORMAL,
                    60, 0, 10, 100, 5),
            createPokemon("CPU One", PokemonType.NORMAL,
                    50, cpuPriority, cpuPower, 100, 5),
            createPokemon("CPU Two", PokemonType.NORMAL,
                    50, cpuPriority, cpuPower, 100, 5),
            createPokemon("CPU Three", PokemonType.NORMAL,
                    50, cpuPriority, cpuPower, 100, 5)
        };
    }

    private static Pokemon createPokemon(String name, PokemonType type,
            int speed, int priority, int power, int accuracy, int pp) {
        Move move = new Move(name + " Move", type, power, accuracy, priority, pp);
        int attack = power >= 100 ? 100 : 50;
        int defense = power >= 100 ? 10 : 50;
        return new Pokemon(name, type, 50, attack, defense, speed,
                new Move[] {move});
    }

    private static boolean containsAnyReference(Pokemon[] first, Pokemon[] second) {
        for (Pokemon firstPokemon : first) {
            for (Pokemon secondPokemon : second) {
                if (firstPokemon == secondPokemon) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsLog(String[] log, String text) {
        for (String line : log) {
            if (line.contains(text)) {
                return true;
            }
        }
        return false;
    }

    private static void checkInvalidIndex(
            BattleGame game, int moveIndex, String failureMessage) {
        boolean rejected = false;
        try {
            game.executeTurn(moveIndex);
        } catch (IndexOutOfBoundsException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void checkTurnRejected(
            BattleGame game, int moveIndex, String failureMessage) {
        boolean rejected = false;
        try {
            game.executeTurn(moveIndex);
        } catch (IllegalStateException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void checkTurnAfterBattleRejected(BattleGame game) {
        checkTurnRejected(game, 0,
                "A turn after the battle has ended should be rejected.");
    }

    private static void checkConstructorRejected(
            Pokemon[] available, Pokemon[] selected, String failureMessage) {
        boolean rejected = false;
        try {
            new BattleGame(available, selected);
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
        checksPassed++;
    }
}
