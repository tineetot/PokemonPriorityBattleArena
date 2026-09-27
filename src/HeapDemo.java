import java.util.Scanner;

public final class HeapDemo {
    private static final int PAGE_COUNT = 5;
    private static final String PAGE_HINT =
            "[A / ENTER] NEXT   [B] BACK   [M] MENU";
    private static final String FINAL_HINT =
            "[A / ENTER] MENU   [B] BACK";

    private final TerminalUI terminalUI;

    public HeapDemo(TerminalUI terminalUI) {
        if (terminalUI == null) {
            throw new IllegalArgumentException("Terminal UI must not be null.");
        }
        this.terminalUI = terminalUI;
    }

    public void run(Scanner scanner) {
        BattleAction[] actions = createSampleActions();
        String[] titles = {
            "WHAT IS A MAX HEAP?",
            "SAMPLE ACTIONS",
            "HEAPIFY-UP",
            "EXTRACTMAX AND HEAPIFY-DOWN",
            "EXECUTION ORDER"
        };
        String[][] pages = createPageContents(actions);
        int pageIndex = 0;

        while (true) {
            String navigationHint = pageIndex == PAGE_COUNT - 1
                    ? FINAL_HINT : PAGE_HINT;
            terminalUI.renderGameBoyPage(
                    titles[pageIndex], (pageIndex + 1) + " / " + PAGE_COUNT,
                    pages[pageIndex], navigationHint);

            String command = terminalUI.readPageCommand(scanner);
            if (command.equalsIgnoreCase("m")) {
                return;
            }
            if (command.equalsIgnoreCase("b")) {
                if (pageIndex == 0) {
                    return;
                }
                pageIndex--;
            } else if (pageIndex == PAGE_COUNT - 1) {
                return;
            } else {
                pageIndex++;
            }
        }
    }

    static BattleAction[] createSampleActions() {
        Move aquaJet = new Move("Aqua Jet", PokemonType.WATER,
                40, 100, 1, 15);
        Move feintAttack = new Move("Feint Attack", PokemonType.NORMAL,
                60, 100, 1, 20);
        Move scratch = new Move("Scratch", PokemonType.NORMAL,
                40, 100, 1, 20);
        Move quickAttack = new Move("Quick Attack", PokemonType.NORMAL,
                40, 100, 2, 15);
        Move vineWhip = new Move("Vine Whip", PokemonType.GRASS,
                45, 100, 0, 25);

        Pokemon squirtle = new Pokemon("Squirtle", PokemonType.WATER,
                105, 48, 65, 43, new Move[] {aquaJet});
        Pokemon vulpix = new Pokemon("Vulpix", PokemonType.FIRE,
                90, 41, 40, 65, new Move[] {feintAttack});
        Pokemon charmander = new Pokemon("Charmander", PokemonType.FIRE,
                95, 52, 43, 65, new Move[] {scratch});
        Pokemon pikachu = new Pokemon("Pikachu", PokemonType.ELECTRIC,
                90, 55, 40, 90, new Move[] {quickAttack});
        Pokemon bulbasaur = new Pokemon("Bulbasaur", PokemonType.GRASS,
                100, 49, 49, 45, new Move[] {vineWhip});

        return new BattleAction[] {
            new BattleAction(squirtle, aquaJet, vulpix, 2),
            new BattleAction(vulpix, feintAttack, pikachu, 4),
            new BattleAction(charmander, scratch, bulbasaur, 1),
            new BattleAction(pikachu, quickAttack, squirtle, 0)
        };
    }

    static BattleAction[] calculateExtractionOrder(BattleAction[] actions) {
        if (actions == null) {
            throw new IllegalArgumentException("Demo actions must not be null.");
        }

        BattleActionMaxHeap heap = new BattleActionMaxHeap();
        for (BattleAction action : actions) {
            heap.insert(action);
        }

        BattleAction[] extractionOrder = new BattleAction[actions.length];
        for (int index = 0; index < extractionOrder.length; index++) {
            extractionOrder[index] = heap.extractMax();
        }
        return extractionOrder;
    }

    private static String[][] createPageContents(BattleAction[] actions) {
        String[][] pages = new String[PAGE_COUNT][];
        pages[0] = new String[] {
            "A max heap chooses the next battle action.",
            "The highest-ranked action stays at the root.",
            "",
            "Ranking rules:",
            "  1. Higher move priority",
            "  2. Higher Pokemon Speed",
            "  3. Lower sequence number",
            "",
            "Sequence means the order actions were selected."
        };

        pages[1] = new String[actions.length * 3 - 1];
        for (int index = 0; index < actions.length; index++) {
            BattleAction action = actions[index];
            int lineIndex = index * 3;
            pages[1][lineIndex] = (index + 1) + ". " + action.getActor().getName()
                    + " uses " + action.getMove().getName()
                    + " -> " + action.getTarget().getName();
            pages[1][lineIndex + 1] = "   Priority: " + action.getMove().getPriority()
                    + "   Speed: " + action.getActor().getSpeed()
                    + "   Sequence: " + action.getSequenceNumber();
            if (lineIndex + 2 < pages[1].length) {
                pages[1][lineIndex + 2] = "";
            }
        }

        BattleActionMaxHeap teachingHeap = new BattleActionMaxHeap();
        for (int index = 0; index < 3; index++) {
            teachingHeap.insert(actions[index]);
        }
        BattleAction[] beforeInsert = teachingHeap.getHeapSnapshot();
        int initialIndex = teachingHeap.size();
        BattleAction parentBeforeInsert = beforeInsert[(initialIndex - 1) / 2];
        teachingHeap.insert(actions[3]);
        BattleAction[] afterInsert = teachingHeap.getHeapSnapshot();
        int finalIndex = findActionIndex(afterInsert, actions[3]);
        pages[2] = buildHeapifyUpPage(
                beforeInsert, afterInsert, initialIndex, finalIndex, parentBeforeInsert);

        BattleAction[] beforeExtract = teachingHeap.getHeapSnapshot();
        BattleAction temporaryRoot = beforeExtract[beforeExtract.length - 1];
        BattleAction removed = teachingHeap.extractMax();
        BattleAction[] afterExtract = teachingHeap.getHeapSnapshot();
        pages[3] = buildHeapifyDownPage(
                beforeExtract, afterExtract, removed, temporaryRoot, teachingHeap.peek());

        BattleActionMaxHeap executionHeap = new BattleActionMaxHeap();
        for (BattleAction action : actions) {
            executionHeap.insert(action);
        }
        BattleAction[] extractionOrder = new BattleAction[actions.length];
        for (int index = 0; index < extractionOrder.length; index++) {
            extractionOrder[index] = executionHeap.extractMax();
        }
        pages[4] = buildExecutionPage(extractionOrder, executionHeap.isEmpty());
        return pages;
    }

    private static String[] buildHeapifyUpPage(BattleAction[] before,
            BattleAction[] after, int initialIndex, int finalIndex,
            BattleAction parentBeforeInsert) {
        String[] lines = new String[15];
        lines[0] = "Pikachu starts at the next open index: " + initialIndex + ".";
        lines[1] = "Quick Attack has Priority 2.";
        lines[2] = "Parent " + parentBeforeInsert.getActor().getName()
                + " has a lower ranking.";
        lines[3] = "Pikachu moves upward to index " + finalIndex + " (the root).";
        lines[4] = "";
        lines[5] = "BEFORE INSERTING PIKACHU";
        copySnapshotLines(before, lines, 6);
        lines[9] = "";
        lines[10] = "AFTER REAL HEAP INSERTION";
        copySnapshotLines(after, lines, 11);
        return lines;
    }

    private static String[] buildHeapifyDownPage(BattleAction[] before,
            BattleAction[] after, BattleAction removed,
            BattleAction temporaryRoot, BattleAction newRoot) {
        String[] lines = new String[15];
        lines[0] = "extractMax removes " + removed.getActor().getName() + " from the root.";
        lines[1] = temporaryRoot.getActor().getName()
                + " temporarily replaces the root.";
        lines[2] = "Heapify-down compares it with both children.";
        lines[3] = newRoot.getActor().getName()
                + " becomes the strongest new root.";
        lines[4] = "";
        lines[5] = "BEFORE extractMax";
        copySnapshotLines(before, lines, 6);
        lines[10] = "";
        lines[11] = "AFTER REAL extractMax";
        copySnapshotLines(after, lines, 12);
        return lines;
    }

    private static String[] buildExecutionPage(
            BattleAction[] extractionOrder, boolean heapEmpty) {
        String[] lines = new String[10];
        for (int index = 0; index < extractionOrder.length; index++) {
            BattleAction action = extractionOrder[index];
            lines[index] = (index + 1) + ". " + action.getActor().getName()
                    + " - " + action.getMove().getName();
        }
        lines[4] = "";
        lines[5] = "Priority -> Speed -> Sequence";
        lines[6] = "";
        lines[7] = "Order produced by the custom max heap.";
        lines[8] = "Heap is empty: " + heapEmpty;
        lines[9] = "No PP was consumed.";
        return lines;
    }

    private static void copySnapshotLines(BattleAction[] snapshot,
            String[] destination, int startIndex) {
        for (int index = 0; index < snapshot.length; index++) {
            destination[startIndex + index] = formatSnapshotRow(index, snapshot[index]);
        }
    }

    private static String formatSnapshotRow(int index, BattleAction action) {
        String row = "[" + index + "] " + padRight(action.getActor().getName(), 11)
                + " P:" + action.getMove().getPriority()
                + " S:" + action.getActor().getSpeed()
                + " Q:" + action.getSequenceNumber();
        if (index == 0) {
            row += " <- ROOT";
        }
        return row;
    }

    private static int findActionIndex(BattleAction[] snapshot, BattleAction action) {
        for (int index = 0; index < snapshot.length; index++) {
            if (snapshot[index] == action) {
                return index;
            }
        }
        return -1;
    }

    private static String padRight(String text, int width) {
        StringBuilder result = new StringBuilder(text);
        while (result.length() < width) {
            result.append(' ');
        }
        return result.toString();
    }
}
