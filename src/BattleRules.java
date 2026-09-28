/** Utility methods for the project's simplified battle calculations. */
public final class BattleRules {
    private BattleRules() {
        // Utility class; no objects are needed.
    }

    public static double getTypeMultiplier(
            PokemonType attackingType, PokemonType defendingType) {
        if (attackingType == null) {
            throw new IllegalArgumentException("Attacking type must not be null.");
        }
        if (defendingType == null) {
            throw new IllegalArgumentException("Defending type must not be null.");
        }

        if ((attackingType == PokemonType.FIRE
                        && defendingType == PokemonType.GRASS)
                || (attackingType == PokemonType.WATER
                        && defendingType == PokemonType.FIRE)
                || (attackingType == PokemonType.GRASS
                        && defendingType == PokemonType.WATER)
                || (attackingType == PokemonType.ELECTRIC
                        && defendingType == PokemonType.WATER)) {
            return 2.0;
        }

        if ((attackingType == PokemonType.FIRE
                        && defendingType == PokemonType.FIRE)
                || (attackingType == PokemonType.FIRE
                        && defendingType == PokemonType.WATER)
                || (attackingType == PokemonType.WATER
                        && defendingType == PokemonType.WATER)
                || (attackingType == PokemonType.WATER
                        && defendingType == PokemonType.GRASS)
                || (attackingType == PokemonType.GRASS
                        && defendingType == PokemonType.GRASS)
                || (attackingType == PokemonType.GRASS
                        && defendingType == PokemonType.FIRE)
                || (attackingType == PokemonType.ELECTRIC
                        && defendingType == PokemonType.ELECTRIC)
                || (attackingType == PokemonType.ELECTRIC
                        && defendingType == PokemonType.GRASS)) {
            return 0.5;
        }

        return 1.0;
    }

    public static boolean doesMoveHit(Move move, int accuracyRoll) {
        if (move == null) {
            throw new IllegalArgumentException("Move must not be null.");
        }
        if (accuracyRoll < 1 || accuracyRoll > 100) {
            throw new IllegalArgumentException(
                    "Accuracy roll must be between 1 and 100.");
        }

        return accuracyRoll <= move.getAccuracy();
    }

    public static int calculateDamage(Pokemon attacker, Pokemon defender, Move move) {
        if (attacker == null) {
            throw new IllegalArgumentException("Attacker must not be null.");
        }
        if (defender == null) {
            throw new IllegalArgumentException("Defender must not be null.");
        }
        if (move == null) {
            throw new IllegalArgumentException("Move must not be null.");
        }
        if (!attackerHasMove(attacker, move)) {
            throw new IllegalArgumentException(
                    "The selected move does not belong to the attacker.");
        }
        if (move.getPower() == 0) {
            return 0;
        }

        // baseDamage = (power * Attack / Defense) / 5.0
        double baseDamage = ((double) move.getPower() * attacker.getAttack()
                / defender.getDefense()) / 5.0;
        double typeMultiplier = getTypeMultiplier(
                move.getType(), defender.getType());
        int finalDamage = (int) Math.round(baseDamage * typeMultiplier);

        // Rounding very small positive results must not erase all damage.
        return Math.max(1, finalDamage);
    }

    private static boolean attackerHasMove(Pokemon attacker, Move selectedMove) {
        for (Move availableMove : attacker.getMoves()) {
            if (availableMove == selectedMove) {
                return true;
            }
        }
        return false;
    }
}
