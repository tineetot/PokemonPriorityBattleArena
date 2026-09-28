public final class TeamSelectionTest {
    private static int checksPassed = 0;

    private TeamSelectionTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        Pokemon[] availablePokemon = createPokemonRoster();
        TeamSelection selection = new TeamSelection(availablePokemon);

        check(selection.getSelectedCount() == 0,
                "A new team selection should start empty.");
        check(!selection.isFull(), "An incomplete team should not be full.");

        selection.addPokemon(2);
        check(selection.getSelectedCount() == 1,
                "The size should increase after the first selection.");
        check(selection.isSelected(availablePokemon[2]),
                "The first Pokemon should be marked as selected.");
        check(selection.isSelected(2),
                "A selected dataset index should be recognized.");

        checkIllegalArgument(new TestOperation() {
            public void run() {
                selection.addPokemon(2);
            }
        }, "A duplicate Pokemon selection should be rejected.");
        check(selection.getSelectedCount() == 1,
                "A rejected duplicate must not change the team size.");

        checkInvalidIndex(new TestOperation() {
            public void run() {
                selection.addPokemon(-1);
            }
        }, "A negative dataset index should be rejected.");
        checkInvalidIndex(new TestOperation() {
            public void run() {
                selection.addPokemon(availablePokemon.length);
            }
        }, "An index beyond the dataset should be rejected.");

        selection.addPokemon(0);
        selection.addPokemon(3);
        check(selection.getSelectedCount() == TeamSelection.TEAM_SIZE,
                "Three valid selections should produce a team of size three.");
        check(selection.isFull(), "A three-Pokemon team should be full.");

        Pokemon[] selectedTeam = selection.getSelectedTeam();
        check(selectedTeam[0] == availablePokemon[2]
                        && selectedTeam[1] == availablePokemon[0]
                        && selectedTeam[2] == availablePokemon[3],
                "Pokemon should remain in selection order.");

        selectedTeam[0] = availablePokemon[1];
        check(selection.getSelectedTeam()[0] == availablePokemon[2],
                "The selected-team getter should return a defensive copy.");

        checkIllegalState(new TestOperation() {
            public void run() {
                selection.addPokemon(1);
            }
        }, "A fourth Pokemon selection should be rejected.");

        Pokemon[] rosterCopyCheck = createPokemonRoster();
        Pokemon originalFirstPokemon = rosterCopyCheck[0];
        TeamSelection protectedSelection = new TeamSelection(rosterCopyCheck);
        rosterCopyCheck[0] = rosterCopyCheck[1];
        protectedSelection.addPokemon(0);
        check(protectedSelection.getSelectedTeam()[0] == originalFirstPokemon,
                "The available roster should be defensively copied.");

        checkIllegalArgument(new TestOperation() {
            public void run() {
                new TeamSelection(null);
            }
        }, "A null dataset should be rejected.");

        final Pokemon[] datasetWithNull = createPokemonRoster();
        datasetWithNull[1] = null;
        checkIllegalArgument(new TestOperation() {
            public void run() {
                new TeamSelection(datasetWithNull);
            }
        }, "A dataset containing a null Pokemon should be rejected.");

        System.out.println("All " + checksPassed
                + " TeamSelection checks passed.");
    }

    private static Pokemon[] createPokemonRoster() {
        return new Pokemon[] {
            createPokemon("Alpha", PokemonType.FIRE, 50),
            createPokemon("Bravo", PokemonType.WATER, 60),
            createPokemon("Charlie", PokemonType.GRASS, 70),
            createPokemon("Delta", PokemonType.ELECTRIC, 80)
        };
    }

    private static Pokemon createPokemon(String name, PokemonType type, int speed) {
        Move move = new Move(name + " Move", type, 40, 100, 0, 10);
        return new Pokemon(name, type, 100, 50, 50, speed, new Move[] {move});
    }

    private static void checkIllegalArgument(TestOperation operation,
            String failureMessage) {
        boolean exceptionThrown = false;
        try {
            operation.run();
        } catch (IllegalArgumentException exception) {
            exceptionThrown = true;
        }
        check(exceptionThrown, failureMessage);
    }

    private static void checkInvalidIndex(TestOperation operation,
            String failureMessage) {
        boolean exceptionThrown = false;
        try {
            operation.run();
        } catch (IndexOutOfBoundsException exception) {
            exceptionThrown = true;
        }
        check(exceptionThrown, failureMessage);
    }

    private static void checkIllegalState(TestOperation operation,
            String failureMessage) {
        boolean exceptionThrown = false;
        try {
            operation.run();
        } catch (IllegalStateException exception) {
            exceptionThrown = true;
        }
        check(exceptionThrown, failureMessage);
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
        checksPassed++;
    }

    private interface TestOperation {
        void run();
    }
}
