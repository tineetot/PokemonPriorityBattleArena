// Class representing the moves in a Pokémon battle

public final class Move {
    private final String name;
    private final PokemonType type;
    private final int power;
    private final int accuracy;
    private final int priority;
    private final int maxPp;
    private int currentPp;

    // Constructor for the Move class, which initializes a move with its name, type, power, accuracy, priority, and maximum PP. It also sets the current PP to the maximum PP.
    public Move(String name, PokemonType type, int power, int accuracy,
            int priority, int maxPp) {
        // Validate the input parameters to ensure they meet the required conditions. If any parameter is invalid, an IllegalArgumentException is thrown with an appropriate message.
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

        // Initialize the instance variables with the provided values. The current PP is set to the maximum PP, indicating that the move starts with full usage capacity.
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.priority = priority;
        this.maxPp = maxPp;
        this.currentPp = maxPp;
    }

    // Getter methods for the Move class, which provide access to the move's properties.
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

    // Override the toString method to provide a string representation of the Move object.
    @Override
    public String toString() {
        return name + " [" + type + ", Power: " + power + ", PP: "
                + currentPp + "/" + maxPp + "]";
    }
}

// The Move class stores a move’s name, type, power, accuracy, priority, and PP. 
// Most fields are final because they should not change during battle. Current PP can change whenever the move is used. 
// The class also validates move data and provides methods for checking, consuming, and restoring PP.