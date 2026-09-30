// Represents one Pokémon, including its HP, Attack, Defense, Speed, type, and available moves.

public final class Pokemon {
    private final String name;
    private final PokemonType type;
    private final int maxHp;
    private int currentHp;
    private final int attack;
    private final int defense;
    private final int speed;
    private final Move[] moves; 

    public Pokemon(String name, PokemonType type, int maxHp, int attack,
            int defense, int speed, Move[] moves) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pokemon name must not be null or blank.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Pokemon type must not be null.");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("Pokemon maximum HP must be greater than zero.");
        }
        if (attack <= 0) {
            throw new IllegalArgumentException("Pokemon Attack must be greater than zero.");
        }
        if (defense <= 0) {
            throw new IllegalArgumentException("Pokemon Defense must be greater than zero.");
        }
        if (speed <= 0) {
            throw new IllegalArgumentException("Pokemon Speed must be greater than zero.");
        }
        validateMoves(moves);

        this.name = name;
        this.type = type;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.moves = moves.clone();
    }

    public String getName() {
        return name;
    }

    public PokemonType getType() {
        return type;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getSpeed() {
        return speed;
    }

    public Move[] getMoves() {
        return moves.clone(); // Return a copy to prevent external modification
    }

    public Move getMove(int index) {
        if (index < 0 || index >= moves.length) {
            throw new IndexOutOfBoundsException(
                    "Move index must be between 0 and " + (moves.length - 1) + ".");
        }
        return moves[index];
    }

    public boolean isFainted() {
        return currentHp == 0;
    }

    public void takeDamage(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Damage amount must not be negative.");
        }

        if (amount >= currentHp) {
            currentHp = 0;
        } else {
            currentHp -= amount;
        }
    }

    public void heal(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Healing amount must not be negative.");
        }

        int missingHp = maxHp - currentHp;
        if (amount >= missingHp) {
            currentHp = maxHp;
        } else {
            currentHp += amount;
        }
    }

    public void restore() {
        currentHp = maxHp;
        for (Move move : moves) {
            move.restorePp();
        }
    }

    @Override
    public String toString() {
        return name + " [" + type + ", HP: " + currentHp + "/" + maxHp
                + ", Speed: " + speed + "]";
    }

    private static void validateMoves(Move[] moves) {
        if (moves == null) {
            throw new IllegalArgumentException("Pokemon moves must not be null.");
        }
        if (moves.length < 1 || moves.length > 4) {
            throw new IllegalArgumentException("A Pokemon must have between one and four moves.");
        }
        for (int index = 0; index < moves.length; index++) {
            if (moves[index] == null) {
                throw new IllegalArgumentException("Move at index " + index + " must not be null.");
            }
        }
    }
}

// The Pokemon class stores each Pokémon’s name, type, HP, Attack, Defense, Speed, and available moves. 
// Its moves are stored in a fixed Move[] array containing between one and four moves. 
// The class manages taking damage, healing, fainting, and restoring HP and PP. 
// Pokémon Speed is also used as the second comparison rule in our max-heap.
