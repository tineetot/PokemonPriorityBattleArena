public final class BattleActionMaxHeapTest {
    private static int checksPassed = 0;

    private BattleActionMaxHeapTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        Move targetMove = new Move("Target Move", PokemonType.NORMAL,
                20, 100, 0, 20);
        Pokemon target = new Pokemon("Targetmon", PokemonType.NORMAL,
                100, 50, 50, 50, new Move[] {targetMove});

        BattleActionMaxHeap emptyHeap = new BattleActionMaxHeap();
        check(emptyHeap.isEmpty(), "A new heap should be empty.");
        check(emptyHeap.size() == 0, "A new heap should have size zero.");

        checkIllegalArgument(new TestOperation() {
            public void run() {
                new BattleActionMaxHeap(0);
            }
        }, "An initial capacity below one should be rejected.");

        checkIllegalArgument(new TestOperation() {
            public void run() {
                emptyHeap.insert(null);
            }
        }, "A null action should be rejected.");

        BattleAction lowAction = createAction("Low", -1, 40, 20, target);
        BattleAction highAction = createAction("High", 3, 20, 30, target);
        emptyHeap.insert(lowAction);
        check(emptyHeap.size() == 1, "One insertion should set size to one.");
        check(emptyHeap.peek() == lowAction, "The only action should be at the root.");
        check(emptyHeap.size() == 1, "Peek should not remove the root action.");
        verifyHeapProperty(emptyHeap);

        emptyHeap.insert(highAction);
        check(emptyHeap.peek() == highAction,
                "Higher move priority should rise to the root.");
        verifyHeapProperty(emptyHeap);

        BattleAction slowerAction = createAction("Slower", 1, 30, 20, target);
        BattleAction fasterAction = createAction("Faster", 1, 90, 30, target);
        BattleActionMaxHeap speedHeap = new BattleActionMaxHeap();
        speedHeap.insert(slowerAction);
        speedHeap.insert(fasterAction);
        check(speedHeap.peek() == fasterAction,
                "Higher Speed should decide tied move priorities.");
        verifyHeapProperty(speedHeap);

        BattleAction laterAction = createAction("Later", 0, 70, 9, target);
        BattleAction earlierAction = createAction("Earlier", 0, 70, 4, target);
        BattleActionMaxHeap sequenceHeap = new BattleActionMaxHeap();
        sequenceHeap.insert(laterAction);
        sequenceHeap.insert(earlierAction);
        check(sequenceHeap.peek() == earlierAction,
                "A lower sequence number should decide tied priority and Speed.");
        verifyHeapProperty(sequenceHeap);

        testMixedExtractionOrder(target);
        testCapacityExpansion(target);
        testRepeatedOperations(target);
        testClearAndReuse(target);
        testSnapshotProtection(target);
        testEqualActions(target);

        checkIllegalState(new TestOperation() {
            public void run() {
                new BattleActionMaxHeap().peek();
            }
        }, "Peeking at an empty heap should be rejected.");

        checkIllegalState(new TestOperation() {
            public void run() {
                new BattleActionMaxHeap().extractMax();
            }
        }, "Extracting from an empty heap should be rejected.");

        int highPpBefore = highAction.getMove().getCurrentPp();
        BattleActionMaxHeap ppHeap = new BattleActionMaxHeap();
        ppHeap.insert(highAction);
        ppHeap.insert(lowAction);
        ppHeap.peek();
        ppHeap.extractMax();
        check(highAction.getMove().getCurrentPp() == highPpBefore,
                "Heap operations must not consume Move PP.");

        System.out.println("All " + checksPassed + " BattleActionMaxHeap checks passed.");
    }

    private static void testMixedExtractionOrder(Pokemon target) {
        BattleAction first = createAction("First", 2, 20, 8, target);
        BattleAction second = createAction("Second", 1, 90, 7, target);
        BattleAction third = createAction("Third", 1, 50, 6, target);
        BattleAction fourth = createAction("Fourth", 0, 80, 3, target);
        BattleAction fifth = createAction("Fifth", 0, 80, 9, target);

        BattleAction[] insertionOrder = {fourth, second, fifth, first, third};
        BattleAction[] expectedOrder = {first, second, third, fourth, fifth};
        BattleActionMaxHeap heap = new BattleActionMaxHeap(2);

        for (BattleAction action : insertionOrder) {
            heap.insert(action);
            verifyHeapProperty(heap);
        }

        int previousSize = heap.size();
        for (BattleAction expected : expectedOrder) {
            check(heap.extractMax() == expected,
                    "Mixed actions should extract in complete priority order.");
            check(heap.size() == previousSize - 1,
                    "Size should decrease by one after extraction.");
            previousSize = heap.size();
            verifyHeapProperty(heap);
        }

        check(heap.isEmpty(), "The heap should be empty after extracting every action.");
    }

    private static void testCapacityExpansion(Pokemon target) {
        BattleActionMaxHeap heap = new BattleActionMaxHeap(1);
        BattleAction[] actions = {
            createAction("Expand A", 0, 20, 5, target),
            createAction("Expand B", 2, 20, 4, target),
            createAction("Expand C", 1, 20, 3, target),
            createAction("Expand D", 3, 20, 2, target),
            createAction("Expand E", -1, 20, 1, target)
        };

        for (BattleAction action : actions) {
            heap.insert(action);
            verifyHeapProperty(heap);
        }

        check(heap.size() == 5,
                "The heap should expand beyond its initial capacity.");

        int previousPriority = Integer.MAX_VALUE;
        while (!heap.isEmpty()) {
            int currentPriority = heap.extractMax().getMove().getPriority();
            check(currentPriority <= previousPriority,
                    "Ordering should remain correct after capacity expansion.");
            previousPriority = currentPriority;
            verifyHeapProperty(heap);
        }
    }

    private static void testRepeatedOperations(Pokemon target) {
        BattleActionMaxHeap heap = new BattleActionMaxHeap();
        BattleAction low = createAction("Repeat Low", 0, 40, 3, target);
        BattleAction middle = createAction("Repeat Middle", 1, 40, 2, target);
        BattleAction high = createAction("Repeat High", 2, 40, 1, target);
        BattleAction newestMaximum = createAction("Repeat Maximum", 4, 40, 4, target);

        heap.insert(low);
        heap.insert(high);
        heap.insert(middle);
        verifyHeapProperty(heap);
        check(heap.extractMax() == high,
                "The current maximum should extract during repeated operations.");
        verifyHeapProperty(heap);
        heap.insert(newestMaximum);
        verifyHeapProperty(heap);
        check(heap.extractMax() == newestMaximum,
                "A newly inserted maximum should extract next.");
        verifyHeapProperty(heap);
        check(heap.extractMax() == middle,
                "Remaining actions should preserve their heap ordering.");
        check(heap.extractMax() == low,
                "The lowest remaining action should extract last.");
        check(heap.isEmpty(), "Repeated insertions and extractions should end cleanly.");
    }

    private static void testClearAndReuse(Pokemon target) {
        BattleActionMaxHeap heap = new BattleActionMaxHeap();
        BattleAction first = createAction("Clear First", 0, 50, 1, target);
        BattleAction reused = createAction("Clear Reused", 1, 50, 2, target);

        heap.insert(first);
        heap.insert(reused);
        heap.clear();
        check(heap.isEmpty(), "Clear should make the heap empty.");
        check(heap.size() == 0, "Clear should reset size to zero.");
        check(heap.getHeapSnapshot().length == 0,
                "Clear should remove every active heap reference.");

        heap.insert(reused);
        check(heap.peek() == reused, "The heap should be reusable after clear.");
        verifyHeapProperty(heap);
    }

    private static void testSnapshotProtection(Pokemon target) {
        BattleActionMaxHeap heap = new BattleActionMaxHeap();
        BattleAction high = createAction("Snapshot High", 2, 40, 1, target);
        BattleAction low = createAction("Snapshot Low", 0, 40, 2, target);
        BattleAction removed = createAction("Snapshot Removed", 3, 40, 3, target);
        heap.insert(low);
        heap.insert(high);
        heap.insert(removed);
        heap.extractMax();

        BattleAction[] snapshot = heap.getHeapSnapshot();
        check(snapshot.length == heap.size(),
                "A snapshot should include only active heap elements.");
        BattleAction rootBeforeChange = heap.peek();
        snapshot[0] = removed;
        check(heap.peek() == rootBeforeChange,
                "Changing a snapshot must not alter the internal heap.");
        verifyHeapProperty(heap);
    }

    private static void testEqualActions(Pokemon target) {
        BattleAction firstEqual = createAction("Equal A", 1, 60, 5, target);
        BattleAction secondEqual = createAction("Equal B", 1, 60, 5, target);
        BattleActionMaxHeap heap = new BattleActionMaxHeap();
        heap.insert(firstEqual);
        heap.insert(secondEqual);
        verifyHeapProperty(heap);

        BattleAction firstResult = heap.extractMax();
        BattleAction secondResult = heap.extractMax();
        check(firstResult != secondResult,
                "Completely equal-ranking actions should both be stored and extracted.");
        check((firstResult == firstEqual || firstResult == secondEqual)
                        && (secondResult == firstEqual || secondResult == secondEqual),
                "Equal-ranking extraction should return the two inserted actions.");
    }

    private static BattleAction createAction(String name, int priority,
            int speed, long sequenceNumber, Pokemon target) {
        Move move = new Move(name + " Move", PokemonType.NORMAL,
                40, 100, priority, 10);
        Pokemon actor = new Pokemon(name + "mon", PokemonType.NORMAL,
                100, 50, 50, speed, new Move[] {move});
        return new BattleAction(actor, move, target, sequenceNumber);
    }

    private static void verifyHeapProperty(BattleActionMaxHeap heap) {
        BattleAction[] snapshot = heap.getHeapSnapshot();
        for (int childIndex = 1; childIndex < snapshot.length; childIndex++) {
            int parentIndex = (childIndex - 1) / 2;
            check(!snapshot[childIndex].hasHigherPriorityThan(snapshot[parentIndex]),
                    "Every child must rank no higher than its parent.");
        }
    }

    private static void checkIllegalArgument(TestOperation operation, String failureMessage) {
        boolean rejected = false;
        try {
            operation.run();
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void checkIllegalState(TestOperation operation, String failureMessage) {
        boolean rejected = false;
        try {
            operation.run();
        } catch (IllegalStateException exception) {
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
