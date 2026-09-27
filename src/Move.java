public final class Move {
    private final String name;
    private final PokemonType type;
    private final int power;
    private final int accuracy;
    private final int priority;
    private final int maxPp;
    private int currentPp;

    public Move(String name, PokemonType type, int power, int accuracy,
            int priority, int maxPp) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Move name must not be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Move type must not be null.");
        }
        if (power < 0) {
            throw new IllegalArgumentException("Move power must not be negative.");
        }
        if (accuracy < 1 || accuracy > 100) {
            throw new IllegalArgumentException("Move accuracy must be between 1 and 100.");
        }
        if (maxPp <= 0) {
            throw new IllegalArgumentException("Move maximum PP must be greater than zero.");
        }

        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.priority = priority;
        this.maxPp = maxPp;
        this.currentPp = maxPp;
    }

    public String getName() {
        return name;
    }

    public PokemonType getType() {
        return type;
    }

    public int getPower() {
        return power;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public int getPriority() {
        return priority;
    }

    public int getMaxPp() {
        return maxPp;
    }

    public int getCurrentPp() {
        return currentPp;
    }

    public boolean hasPp() {
        return currentPp > 0;
    }

    public boolean usePp() {
        if (!hasPp()) {
            return false;
        }

        currentPp--;
        return true;
    }

    public void restorePp() {
        currentPp = maxPp;
    }

    @Override
    public String toString() {
        return name + " [" + type + ", Power: " + power + ", PP: "
                + currentPp + "/" + maxPp + "]";
    }
}
