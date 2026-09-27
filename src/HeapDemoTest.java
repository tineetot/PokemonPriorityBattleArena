public final class HeapDemoTest {
    private static int checksPassed = 0;

    private HeapDemoTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        BattleAction[] actions = HeapDemo.createSampleActions();
        check(actions.length == 4, "The demonstration should create exactly four actions.");
        check(hasActor(actions, "Squirtle"), "The samples should include Squirtle.");
        check(hasActor(actions, "Vulpix"), "The samples should include Vulpix.");
        check(hasActor(actions, "Charmander"), "The samples should include Charmander.");
        check(hasActor(actions, "Pikachu"), "The samples should include Pikachu.");

        BattleAction[] originalOrder = actions.clone();
        int[] ppBefore = new int[actions.length];

        for (int index = 0; index < actions.length; index++) {
            BattleAction action = actions[index];
            check(action != null, "No sample action should be null.");
            check(action.getActor() != null, "Every sample actor should be valid.");
            check(action.getMove() != null, "Every sample move should be valid.");
            check(action.getTarget() != null, "Every sample target should be valid.");
            check(actorOwnsMove(action), "Every selected move should belong to its actor.");
            check(action.getActor() != action.getTarget(),
                    "No sample actor should target itself.");
            check(action.getSequenceNumber() >= 0,
                    "Every sample sequence number should be non-negative.");
            ppBefore[index] = action.getMove().getCurrentPp();

            for (int otherIndex = index + 1; otherIndex < actions.length; otherIndex++) {
                check(action.getSequenceNumber() != actions[otherIndex].getSequenceNumber(),
                        "Sample sequence numbers should be unique.");
            }
        }

        check(hasMovePriorityComparison(actions),
                "The samples should demonstrate different move priorities.");
        check(hasSpeedTieBreaker(actions),
                "The samples should demonstrate a Speed comparison.");
        check(hasSequenceTieBreaker(actions),
                "The samples should demonstrate a sequence-number comparison.");

        BattleAction[] extractionOrder = HeapDemo.calculateExtractionOrder(actions);
        check(extractionOrder.length == 4,
                "The custom heap should extract all four actions.");
        String[] expectedActors = {"Pikachu", "Charmander", "Vulpix", "Squirtle"};
        for (int index = 0; index < expectedActors.length; index++) {
            check(extractionOrder[index].getActor().getName().equals(expectedActors[index]),
                    "Extraction order should place " + expectedActors[index]
                    + " at position " + (index + 1) + ".");
        }

        for (int index = 1; index < extractionOrder.length; index++) {
            check(extractionOrder[index - 1].comparePriorityTo(extractionOrder[index]) >= 0,
                    "Adjacent extracted actions should follow BattleAction priority.");
        }

        BattleActionMaxHeap emptinessCheck = new BattleActionMaxHeap();
        for (BattleAction action : actions) {
            emptinessCheck.insert(action);
        }
        while (!emptinessCheck.isEmpty()) {
            emptinessCheck.extractMax();
        }
        check(emptinessCheck.isEmpty(), "The heap should be empty after all extractions.");

        for (int index = 0; index < actions.length; index++) {
            check(actions[index] == originalOrder[index],
                    "Calculating extraction order must not modify the original array.");
            check(actions[index].getMove().getCurrentPp() == ppBefore[index],
                    "Demonstration logic must not consume Move PP.");
        }

        System.out.println("All " + checksPassed + " HeapDemo checks passed.");
    }

    private static boolean hasActor(BattleAction[] actions, String actorName) {
        for (BattleAction action : actions) {
            if (action.getActor().getName().equals(actorName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean actorOwnsMove(BattleAction action) {
        for (Move availableMove : action.getActor().getMoves()) {
            if (availableMove == action.getMove()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMovePriorityComparison(BattleAction[] actions) {
        for (int first = 0; first < actions.length; first++) {
            for (int second = first + 1; second < actions.length; second++) {
                if (actions[first].getMove().getPriority()
                        != actions[second].getMove().getPriority()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasSpeedTieBreaker(BattleAction[] actions) {
        for (int first = 0; first < actions.length; first++) {
            for (int second = first + 1; second < actions.length; second++) {
                if (actions[first].getMove().getPriority()
                                == actions[second].getMove().getPriority()
                        && actions[first].getActor().getSpeed()
                                != actions[second].getActor().getSpeed()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasSequenceTieBreaker(BattleAction[] actions) {
        for (int first = 0; first < actions.length; first++) {
            for (int second = first + 1; second < actions.length; second++) {
                if (actions[first].getMove().getPriority()
                                == actions[second].getMove().getPriority()
                        && actions[first].getActor().getSpeed()
                                == actions[second].getActor().getSpeed()
                        && actions[first].getSequenceNumber()
                                != actions[second].getSequenceNumber()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
        checksPassed++;
    }
}
