import java.util.Random;

/** Manages one complete three-versus-three battle. */
public final class BattleGame {
    private static final int TEAM_SIZE = 3;
    private static final int MAX_LOG_LINES = 25;

    private final Pokemon[] playerTeam;
    private final Pokemon[] cpuTeam;
    private int activePlayerIndex;
    private int activeCpuIndex;
    private final BattleActionMaxHeap actionHeap;
    private int turnNumber;
    private long nextSequenceNumber;
    private final Random random;
    private final String[] lastTurnLog;
    private int lastTurnLogCount;
    private final String[] lastTurnOrder;
    private int lastTurnOrderCount;

    public BattleGame(Pokemon[] availablePokemon, Pokemon[] playerTeam) {
        validateAvailablePokemon(availablePokemon);
        validatePlayerTeam(availablePokemon, playerTeam);

        this.playerTeam = playerTeam.clone();
        this.cpuTeam = buildCpuTeam(availablePokemon, playerTeam);
        this.activePlayerIndex = 0;
        this.activeCpuIndex = 0;
        this.actionHeap = new BattleActionMaxHeap();
        this.turnNumber = 1;
        this.nextSequenceNumber = 0;
        this.random = new Random();
        this.lastTurnLog = new String[MAX_LOG_LINES];
        this.lastTurnLogCount = 0;
        this.lastTurnOrder = new String[2];
        this.lastTurnOrderCount = 0;
    }

    public Pokemon[] getPlayerTeam() {
        return playerTeam.clone();
    }

    public Pokemon[] getCpuTeam() {
        return cpuTeam.clone();
    }

    public Pokemon getActivePlayerPokemon() {
        return playerTeam[activePlayerIndex];
    }

    public Pokemon getActiveCpuPokemon() {
        return cpuTeam[activeCpuIndex];
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public String[] getLastTurnLog() {
        String[] occupiedLines = new String[lastTurnLogCount];
        for (int index = 0; index < lastTurnLogCount; index++) {
            occupiedLines[index] = lastTurnLog[index];
        }
        return occupiedLines;
    }

    public String[] getLastTurnOrder() {
        String[] occupiedOrder = new String[lastTurnOrderCount];
        for (int index = 0; index < lastTurnOrderCount; index++) {
            occupiedOrder[index] = lastTurnOrder[index];
        }
        return occupiedOrder;
    }

    public boolean isActionHeapEmpty() {
        return actionHeap.isEmpty();
    }

    public boolean isBattleOver() {
        return allFainted(playerTeam) || allFainted(cpuTeam);
    }

    public boolean didPlayerWin() {
        return allFainted(cpuTeam);
    }

    public boolean didCpuWin() {
        return allFainted(playerTeam) && !allFainted(cpuTeam);
    }

    public void executeTurn(int playerMoveIndex) {
        if (isBattleOver()) {
            throw new IllegalStateException("The battle has already ended.");
        }

        Pokemon playerPokemon = getActivePlayerPokemon();
        Pokemon cpuPokemon = getActiveCpuPokemon();
        if (playerPokemon.isFainted()) {
            throw new IllegalStateException(
                    "The active player Pokemon has fainted and cannot act.");
        }
        if (playerMoveIndex < 0 || playerMoveIndex >= playerPokemon.getMoves().length) {
            throw new IndexOutOfBoundsException(
                    "Player move index must be between 0 and "
                            + (playerPokemon.getMoves().length - 1) + ".");
        }

        Move playerMove = playerPokemon.getMove(playerMoveIndex);
        if (!playerMove.hasPp()) {
            throw new IllegalStateException(
                    playerMove.getName() + " has no PP remaining.");
        }

        clearLatestTurnData();
        addLog("Turn " + turnNumber);
        addLog("Player chose " + playerPokemon.getName() + " - "
                + playerMove.getName() + ".");

        Move cpuMove = chooseCpuMove(cpuPokemon);
        if (cpuMove == null) {
            addLog(cpuPokemon.getName() + " has no move with PP remaining.");
        } else {
            addLog("CPU chose " + cpuPokemon.getName() + " - "
                    + cpuMove.getName() + ".");
        }

        BattleAction playerAction = new BattleAction(
                playerPokemon, playerMove, cpuPokemon, nextSequenceNumber++);
        actionHeap.insert(playerAction);

        if (cpuMove != null) {
            BattleAction cpuAction = new BattleAction(
                    cpuPokemon, cpuMove, playerPokemon, nextSequenceNumber++);
            actionHeap.insert(cpuAction);
        }

        while (!actionHeap.isEmpty()) {
            BattleAction action = actionHeap.extractMax();
            recordTurnOrder(action);
            executeAction(action);
        }

        replaceFaintedPokemon();
        if (didPlayerWin()) {
            addLog("Player wins the battle!");
        } else if (didCpuWin()) {
            addLog("CPU wins the battle!");
        }

        turnNumber++;
        if (!actionHeap.isEmpty()) {
            throw new IllegalStateException("The action heap should be empty after a turn.");
        }
    }

    private Move chooseCpuMove(Pokemon cpuPokemon) {
        for (Move move : cpuPokemon.getMoves()) {
            if (move.hasPp()) {
                return move;
            }
        }
        return null;
    }

    private void executeAction(BattleAction action) {
        Pokemon actor = action.getActor();
        Pokemon target = action.getTarget();
        Move move = action.getMove();

        if (actor.isFainted()) {
            addLog(actor.getName() + " fainted before acting, so its action was skipped.");
            return;
        }
        if (target.isFainted()) {
            addLog(target.getName() + " had already fainted, so "
                    + actor.getName() + "'s action was skipped.");
            return;
        }
        if (!move.hasPp()) {
            addLog(actor.getName() + " could not use " + move.getName()
                    + " because it has no PP remaining.");
            return;
        }

        move.usePp();
        int accuracyRoll = random.nextInt(100) + 1;
        if (!BattleRules.doesMoveHit(move, accuracyRoll)) {
            addLog(actor.getName() + " used " + move.getName() + ", but it missed.");
            return;
        }

        int damage = BattleRules.calculateDamage(actor, target, move);
        target.takeDamage(damage);
        addLog(actor.getName() + " used " + move.getName() + " for "
                + damage + " damage. " + target.getName() + " HP: "
                + target.getCurrentHp() + "/" + target.getMaxHp() + ".");

        double multiplier = BattleRules.getTypeMultiplier(
                move.getType(), target.getType());
        if (multiplier == 2.0) {
            addLog("It's super effective!");
        } else if (multiplier == 0.5) {
            addLog("It's not very effective.");
        }

        if (target.isFainted()) {
            addLog(target.getName() + " fainted.");
        }
    }

    private void recordTurnOrder(BattleAction action) {
        String orderLine = action.getActor().getName() + " - "
                + action.getMove().getName();
        lastTurnOrder[lastTurnOrderCount] = orderLine;
        lastTurnOrderCount++;
        addLog("Heap extracted: " + orderLine + ".");
    }

    private void replaceFaintedPokemon() {
        if (getActivePlayerPokemon().isFainted() && !allFainted(playerTeam)) {
            activePlayerIndex = findNextPokemon(playerTeam, activePlayerIndex + 1);
            addLog("Player sends out " + getActivePlayerPokemon().getName() + ".");
        }
        if (getActiveCpuPokemon().isFainted() && !allFainted(cpuTeam)) {
            activeCpuIndex = findNextPokemon(cpuTeam, activeCpuIndex + 1);
            addLog("CPU sends out " + getActiveCpuPokemon().getName() + ".");
        }
    }

    private int findNextPokemon(Pokemon[] team, int startIndex) {
        for (int index = startIndex; index < team.length; index++) {
            if (!team[index].isFainted()) {
                return index;
            }
        }
        throw new IllegalStateException("No replacement Pokemon is available.");
    }

    private void clearLatestTurnData() {
        actionHeap.clear();
        for (int index = 0; index < lastTurnLog.length; index++) {
            lastTurnLog[index] = null;
        }
        for (int index = 0; index < lastTurnOrder.length; index++) {
            lastTurnOrder[index] = null;
        }
        lastTurnLogCount = 0;
        lastTurnOrderCount = 0;
    }

    private void addLog(String message) {
        if (lastTurnLogCount >= lastTurnLog.length) {
            throw new IllegalStateException("The latest turn log is full.");
        }
        lastTurnLog[lastTurnLogCount] = message;
        lastTurnLogCount++;
    }

    private static Pokemon[] buildCpuTeam(
            Pokemon[] availablePokemon, Pokemon[] playerTeam) {
        Pokemon[] cpuTeam = new Pokemon[TEAM_SIZE];
        int cpuCount = 0;

        for (Pokemon pokemon : availablePokemon) {
            if (!containsReference(playerTeam, playerTeam.length, pokemon)
                    && !containsReference(cpuTeam, cpuCount, pokemon)) {
                cpuTeam[cpuCount] = pokemon;
                cpuCount++;
                if (cpuCount == TEAM_SIZE) {
                    return cpuTeam;
                }
            }
        }

        throw new IllegalArgumentException(
                "At least three Pokemon must remain for the CPU team.");
    }

    private static void validateAvailablePokemon(Pokemon[] availablePokemon) {
        if (availablePokemon == null) {
            throw new IllegalArgumentException("Available Pokemon must not be null.");
        }
        for (int index = 0; index < availablePokemon.length; index++) {
            if (availablePokemon[index] == null) {
                throw new IllegalArgumentException(
                        "Available Pokemon at index " + index + " must not be null.");
            }
        }
    }

    private static void validatePlayerTeam(
            Pokemon[] availablePokemon, Pokemon[] playerTeam) {
        if (playerTeam == null || playerTeam.length != TEAM_SIZE) {
            throw new IllegalArgumentException(
                    "The player team must contain exactly three Pokemon.");
        }

        for (int index = 0; index < playerTeam.length; index++) {
            Pokemon pokemon = playerTeam[index];
            if (pokemon == null) {
                throw new IllegalArgumentException(
                        "Player Pokemon at index " + index + " must not be null.");
            }
            if (!containsReference(availablePokemon, availablePokemon.length, pokemon)) {
                throw new IllegalArgumentException(
                        pokemon.getName() + " is not from the available Pokemon array.");
            }
            if (containsReference(playerTeam, index, pokemon)) {
                throw new IllegalArgumentException(
                        "The player team must not contain duplicate Pokemon.");
            }
        }
    }

    private static boolean containsReference(
            Pokemon[] pokemon, int usedLength, Pokemon target) {
        for (int index = 0; index < usedLength; index++) {
            if (pokemon[index] == target) {
                return true;
            }
        }
        return false;
    }

    private static boolean allFainted(Pokemon[] team) {
        for (Pokemon pokemon : team) {
            if (!pokemon.isFainted()) {
                return false;
            }
        }
        return true;
    }
}
