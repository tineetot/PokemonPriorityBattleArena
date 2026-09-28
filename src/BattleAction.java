// Class representing a single action taken by a Pokemon in battle, including the acting Pokemon, the move used, the target Pokemon, and the sequence number of the action.

public final class BattleAction {
    private final Pokemon actor;
    private final Move move;
    private final Pokemon target;
    private final long sequenceNumber;

    public BattleAction(Pokemon actor, Move move, Pokemon target, long sequenceNumber) {
        if (actor == null) {
            throw new IllegalArgumentException("Battle action actor must not be null.");
        }
        if (move == null) {
            throw new IllegalArgumentException("Battle action move must not be null.");
        }
        if (target == null) {
            throw new IllegalArgumentException("Battle action target must not be null.");
        }
        if (sequenceNumber < 0) {
            throw new IllegalArgumentException("Battle action sequence number must not be negative.");
        }
        if (actor == target) {
            throw new IllegalArgumentException("A Pokemon cannot target itself with this battle action.");
        }
        if (!actorHasMove(actor, move)) {
            throw new IllegalArgumentException("The selected move does not belong to the acting Pokemon.");
        }

        this.actor = actor;
        this.move = move;
        this.target = target;
        this.sequenceNumber = sequenceNumber;
    }

    public Pokemon getActor() {
        return actor;
    }

    public Move getMove() {
        return move;
    }

    public Pokemon getTarget() {
        return target;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean hasHigherPriorityThan(BattleAction other) {
        return comparePriorityTo(other) > 0;
    }

    // Compares this BattleAction to another BattleAction based on move priority, actor speed, and sequence number.
    public int comparePriorityTo(BattleAction other) {
        if (other == null) {
            throw new IllegalArgumentException("Other battle action must not be null.");
        }

        int priorityComparison = Integer.compare(
                move.getPriority(), other.move.getPriority());
        if (priorityComparison != 0) {
            return priorityComparison;
        }

        int speedComparison = Integer.compare(
                actor.getSpeed(), other.actor.getSpeed());
        if (speedComparison != 0) {
            return speedComparison;
        }

        // If both priority and speed are equal, compare by sequence number (lower sequence number goes first).
        return Long.compare(other.sequenceNumber, sequenceNumber);
    }

    @Override
    public String toString() {
        return actor.getName() + " uses " + move.getName() + " on " + target.getName()
                + " [priority=" + move.getPriority()
                + ", speed=" + actor.getSpeed()
                + ", sequence=" + sequenceNumber + "]";
    }

    private static boolean actorHasMove(Pokemon actor, Move selectedMove) {
        for (Move availableMove : actor.getMoves()) {
            if (availableMove == selectedMove) {
                return true;
            }
        }
        return false;
    }
}

// A BattleAction represents one Pokémon using one selected move on a target. 
// It stores the actor, move, target, and sequence number. The class also defines how actions are compared. 
// Higher move priority comes first, followed by higher Pokémon Speed, and then lower sequence number as the final tie-breaker