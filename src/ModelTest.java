public final class ModelTest {
    private static int checksPassed = 0;

    private ModelTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        Move quickStrike = new Move("Quick Strike", PokemonType.NORMAL, 40, 100, 1, 2);
        Move ember = new Move("Ember", PokemonType.FIRE, 40, 100, 0, 5);

        check(quickStrike.getCurrentPp() == quickStrike.getMaxPp(),
                "A move should begin with full PP.");
        check(quickStrike.usePp(), "Using an available move should consume PP.");
        check(quickStrike.getCurrentPp() == 1, "Using a move should decrease PP by one.");
        check(quickStrike.usePp(), "The final available PP should be usable.");
        check(!quickStrike.usePp(), "A move with zero PP should not be usable.");
        check(quickStrike.getCurrentPp() == 0, "PP must not fall below zero.");
        quickStrike.restorePp();
        check(quickStrike.getCurrentPp() == quickStrike.getMaxPp(),
                "Restoring a move should return it to full PP.");

        Pokemon testPokemon = new Pokemon("Testmon", PokemonType.FIRE, 100,
                55, 45, 60, new Move[] {quickStrike, ember});

        check(testPokemon.getCurrentHp() == testPokemon.getMaxHp(),
                "A Pokemon should begin with full HP.");
        testPokemon.takeDamage(30);
        check(testPokemon.getCurrentHp() == 70, "Damage should decrease current HP.");
        testPokemon.takeDamage(500);
        check(testPokemon.getCurrentHp() == 0, "Damage must not reduce HP below zero.");
        check(testPokemon.isFainted(), "A Pokemon at zero HP should be fainted.");
        testPokemon.heal(500);
        check(testPokemon.getCurrentHp() == testPokemon.getMaxHp(),
                "Healing must not exceed maximum HP.");

        quickStrike.usePp();
        testPokemon.takeDamage(20);
        testPokemon.restore();
        check(testPokemon.getCurrentHp() == testPokemon.getMaxHp(),
                "Restore should return HP to maximum.");
        check(quickStrike.getCurrentPp() == quickStrike.getMaxPp(),
                "Restore should replenish every move's PP.");
        check(testPokemon.getMove(0) == quickStrike,
                "A move should be retrievable by its array index.");

        Move[] copiedMoves = testPokemon.getMoves();
        copiedMoves[0] = ember;
        check(testPokemon.getMove(0) == quickStrike,
                "The moves getter should protect the internal array.");

        boolean invalidMoveRejected = false;
        try {
            new Move(" ", PokemonType.NORMAL, 40, 100, 0, 10);
        } catch (IllegalArgumentException exception) {
            invalidMoveRejected = true;
        }
        check(invalidMoveRejected, "An invalid move constructor input should be rejected.");

        boolean invalidIndexRejected = false;
        try {
            testPokemon.getMove(2);
        } catch (IndexOutOfBoundsException exception) {
            invalidIndexRejected = true;
        }
        check(invalidIndexRejected, "An invalid move index should be rejected.");

        System.out.println("All " + checksPassed + " model checks passed.");
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
        checksPassed++;
    }
}
