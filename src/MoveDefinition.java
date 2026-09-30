// Stores a move template loaded from the CSV and creates separate Move objects so Pokémon do not share current PP.

public final class MoveDefinition {
    private final String id;
    private final String name;
    private final PokemonType type;
    private final int power;
    private final int accuracy;
    private final int priority;
    private final int maxPp;

    public MoveDefinition(String id, String name, PokemonType type, int power,
            int accuracy, int priority, int maxPp) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Move ID must not be null or blank.");
        }
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

        this.id = id;
        this.name = name;
        this.type = type;
        this.power = power;
        this.accuracy = accuracy;
        this.priority = priority;
        this.maxPp = maxPp;
    }

    public String getId() {
        return id;
    }

    public Move createMove() {
        return new Move(name, type, power, accuracy, priority, maxPp);
    }
}
