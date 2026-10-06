public final class BattleActionTest {
    private static int checksPassed = 0;

    private BattleActionTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        Move priorityMove = new Move("Priority Burst", PokemonType.ELECTRIC,
                40, 100, 2, 5);
        Move fastMove = new Move("Fast Strike", PokemonType.NORMAL,
                40, 100, 0, 10);
        Move tiedMove = new Move("Tied Strike", PokemonType.GRASS,
                40, 100, 0, 10);
        Move targetMove = new Move("Guard Tap", PokemonType.WATER,
                20, 100, 0, 15);

        Pokemon slowActor = new Pokemon("Slowmon", PokemonType.ELECTRIC,
                100, 50, 50, 30, new Move[] {priorityMove});
        Pokemon fastActor = new Pokemon("Fastmon", PokemonType.NORMAL,
                100, 50, 50, 100, new Move[] {fastMove});
        Pokemon tiedActor = new Pokemon("Tiedmon", PokemonType.GRASS,
                100, 50, 50, 100, new Move[] {tiedMove});
        Pokemon target = new Pokemon("Targetmon", PokemonType.WATER,
                100, 50, 50, 50, new Move[] {targetMove});

        int priorityPpBefore = priorityMove.getCurrentPp();
        int fastPpBefore = fastMove.getCurrentPp();

        BattleAction priorityAction = new BattleAction(
                slowActor, priorityMove, target, 20);
        BattleAction fastAction = new BattleAction(
                fastActor, fastMove, target, 20);

        check(priorityAction.comparePriorityTo(fastAction) > 0,
                "Higher move priority should win even when its actor is slower.");
        check(fastAction.comparePriorityTo(priorityAction) < 0,
                "The lower-priority action should compare below the higher-priority action.");

        BattleAction tiedSpeedAction = new BattleAction(
                tiedActor, tiedMove, target, 30);
        BattleAction slowerStandardAction = new BattleAction(
                target, targetMove, slowActor, 30);
        check(tiedSpeedAction.comparePriorityTo(slowerStandardAction) > 0,
                "Higher Speed should win when move priorities are tied.");

        BattleAction earlierAction = new BattleAction(
                tiedActor, tiedMove, target, 10);
        BattleAction laterAction = new BattleAction(
                fastActor, fastMove, target, 11);
        check(earlierAction.comparePriorityTo(laterAction) > 0,
                "A lower sequence number should win when priority and Speed are tied.");

        BattleAction equalActionOne = new BattleAction(
                tiedActor, tiedMove, target, 7);
        BattleAction equalActionTwo = new BattleAction(
                fastActor, fastMove, target, 7);
        check(equalActionOne.comparePriorityTo(equalActionTwo) == 0,
                "Equal ranking values should compare as equal.");

        check(priorityAction.hasHigherPriorityThan(fastAction)
                        == (priorityAction.comparePriorityTo(fastAction) > 0),
                "hasHigherPriorityThan should agree with comparePriorityTo.");
        check(!equalActionOne.hasHigherPriorityThan(equalActionTwo),
                "An equally ranked action should not count as higher priority.");

        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(null, priorityMove, target, 0);
            }
        }, "A null actor should be rejected.");

        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(slowActor, null, target, 0);
            }
        }, "A null move should be rejected.");

        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(slowActor, priorityMove, null, 0);
            }
        }, "A null target should be rejected.");

        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(slowActor, priorityMove, target, -1);
            }
        }, "A negative sequence number should be rejected.");

        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(slowActor, priorityMove, slowActor, 0);
            }
        }, "An actor targeting the exact same object should be rejected.");

        Move unownedMove = new Move("Unowned Move", PokemonType.FIRE,
                30, 100, 0, 5);
        checkRejected(new TestOperation() {
            public void run() {
                new BattleAction(slowActor, unownedMove, target, 0);
            }
        }, "A move not owned by the actor should be rejected.");

        check(priorityMove.getCurrentPp() == priorityPpBefore,
                "Creating and comparing actions must not consume the priority move's PP.");
        check(fastMove.getCurrentPp() == fastPpBefore,
                "Creating and comparing actions must not consume the standard move's PP.");

        check(priorityAction.getActor() == slowActor,
                "The actor getter should return the selected actor.");
        check(priorityAction.getMove() == priorityMove,
                "The move getter should return the selected move.");
        check(priorityAction.getTarget() == target,
                "The target getter should return the selected target.");
        check(priorityAction.getSequenceNumber() == 20,
                "The sequence-number getter should return the selected value.");

        String actionText = priorityAction.toString();
        check(actionText.contains("Slowmon"), "toString should include the actor name.");
        check(actionText.contains("Priority Burst"), "toString should include the move name.");
        check(actionText.contains("Targetmon"), "toString should include the target name.");
        check(actionText.contains("priority=2"), "toString should include move priority.");
        check(actionText.contains("speed=30"), "toString should include actor Speed.");
        check(actionText.contains("sequence=20"), "toString should include the sequence number.");

        System.out.println("All " + checksPassed + " BattleAction checks passed.");
    }

    private static void checkRejected(TestOperation operation, String failureMessage) {
        boolean rejected = false;
        try {
            operation.run();
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

    private interface TestOperation {
        void run();
    }
}
