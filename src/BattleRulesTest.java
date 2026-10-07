public final class BattleRulesTest {
    private static int checksPassed = 0;

    private BattleRulesTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) {
        testTypeEffectiveness();
        testAccuracy();
        testDamage();

        System.out.println("All " + checksPassed + " BattleRules checks passed.");
    }

    private static void testTypeEffectiveness() {
        check(BattleRules.getTypeMultiplier(PokemonType.FIRE, PokemonType.GRASS) == 2.0,
                "Fire should be super effective against Grass.");
        check(BattleRules.getTypeMultiplier(PokemonType.FIRE, PokemonType.WATER) == 0.5,
                "Fire should be resisted by Water.");
        check(BattleRules.getTypeMultiplier(PokemonType.WATER, PokemonType.FIRE) == 2.0,
                "Water should be super effective against Fire.");
        check(BattleRules.getTypeMultiplier(PokemonType.GRASS, PokemonType.WATER) == 2.0,
                "Grass should be super effective against Water.");
        check(BattleRules.getTypeMultiplier(PokemonType.ELECTRIC, PokemonType.WATER) == 2.0,
                "Electric should be super effective against Water.");

        for (PokemonType defendingType : PokemonType.values()) {
            check(BattleRules.getTypeMultiplier(PokemonType.NORMAL, defendingType) == 1.0,
                    "Normal should be neutral against " + defendingType + ".");
        }

        check(BattleRules.getTypeMultiplier(PokemonType.WATER, PokemonType.ELECTRIC) == 1.0,
                "Water against Electric should be neutral.");
        check(BattleRules.getTypeMultiplier(PokemonType.GRASS, PokemonType.ELECTRIC) == 1.0,
                "Grass against Electric should be neutral.");
        check(BattleRules.getTypeMultiplier(PokemonType.FIRE, PokemonType.FIRE) == 0.5,
                "Fire should resist Fire.");
        check(BattleRules.getTypeMultiplier(PokemonType.WATER, PokemonType.WATER) == 0.5,
                "Water should resist Water.");
        check(BattleRules.getTypeMultiplier(PokemonType.GRASS, PokemonType.GRASS) == 0.5,
                "Grass should resist Grass.");
        check(BattleRules.getTypeMultiplier(
                        PokemonType.ELECTRIC, PokemonType.ELECTRIC) == 0.5,
                "Electric should resist Electric.");

        checkTypeRejected(null, PokemonType.FIRE,
                "A null attacking type should be rejected.");
        checkTypeRejected(PokemonType.FIRE, null,
                "A null defending type should be rejected.");
    }

    private static void testAccuracy() {
        Move focusedMove = new Move("Focused Move", PokemonType.NORMAL,
                40, 75, 0, 10);
        int ppBeforeChecks = focusedMove.getCurrentPp();

        check(BattleRules.doesMoveHit(focusedMove, 75),
                "A roll equal to the move's accuracy should hit.");
        check(BattleRules.doesMoveHit(focusedMove, 74),
                "A roll below the move's accuracy should hit.");
        check(!BattleRules.doesMoveHit(focusedMove, 76),
                "A roll above the move's accuracy should miss.");

        Move certainMove = new Move("Certain Move", PokemonType.NORMAL,
                40, 100, 0, 10);
        check(BattleRules.doesMoveHit(certainMove, 100),
                "A 100-accuracy move should hit on a roll of 100.");

        checkAccuracyRejected(focusedMove, 0,
                "An accuracy roll of 0 should be rejected.");
        checkAccuracyRejected(focusedMove, 101,
                "An accuracy roll of 101 should be rejected.");
        checkAccuracyRejected(null, 50,
                "A null move should be rejected during an accuracy check.");
        check(focusedMove.getCurrentPp() == ppBeforeChecks,
                "Accuracy checks must not consume PP.");
    }

    private static void testDamage() {
        Move fireMove = new Move("Test Flame", PokemonType.FIRE,
                40, 100, 0, 10);
        Pokemon attacker = createPokemon(
                "Attacker", PokemonType.FIRE, 50, 50, fireMove);
        Pokemon neutralDefender = createPokemon(
                "Neutral Defender", PokemonType.ELECTRIC, 50, 50,
                createMove("Neutral Defense Move", PokemonType.ELECTRIC, 20));
        Pokemon weakDefender = createPokemon(
                "Weak Defender", PokemonType.GRASS, 50, 50,
                createMove("Weak Defense Move", PokemonType.GRASS, 20));
        Pokemon resistantDefender = createPokemon(
                "Resistant Defender", PokemonType.WATER, 50, 50,
                createMove("Resistant Defense Move", PokemonType.WATER, 20));

        int neutralDamage = BattleRules.calculateDamage(
                attacker, neutralDefender, fireMove);
        int superEffectiveDamage = BattleRules.calculateDamage(
                attacker, weakDefender, fireMove);
        int resistedDamage = BattleRules.calculateDamage(
                attacker, resistantDefender, fireMove);

        check(neutralDamage == 8,
                "Neutral damage should follow the documented formula.");
        check(superEffectiveDamage > neutralDamage,
                "Super-effective damage should exceed comparable neutral damage.");
        check(resistedDamage < neutralDamage,
                "Resisted damage should be below comparable neutral damage.");
        check(superEffectiveDamage == 16,
                "The 2.0 multiplier should double the comparable damage.");
        check(resistedDamage == 4,
                "The 0.5 multiplier should halve the comparable damage.");

        Move tinyMove = createMove("Tiny Move", PokemonType.NORMAL, 1);
        Pokemon weakAttacker = createPokemon(
                "Weak Attacker", PokemonType.NORMAL, 1, 50, tinyMove);
        Pokemon strongDefender = createPokemon(
                "Strong Defender", PokemonType.NORMAL, 50, 1000,
                createMove("Strong Defense Move", PokemonType.NORMAL, 20));
        check(BattleRules.calculateDamage(weakAttacker, strongDefender, tinyMove) == 1,
                "A positive-power move should deal at least one damage.");

        Move zeroPowerMove = createMove("Zero Power", PokemonType.NORMAL, 0);
        Pokemon zeroPowerAttacker = createPokemon(
                "Zero Attacker", PokemonType.NORMAL, 50, 50, zeroPowerMove);
        check(BattleRules.calculateDamage(
                        zeroPowerAttacker, neutralDefender, zeroPowerMove) == 0,
                "A zero-power move should deal zero damage.");

        int defenderHpBefore = neutralDefender.getCurrentHp();
        int movePpBefore = fireMove.getCurrentPp();
        BattleRules.calculateDamage(attacker, neutralDefender, fireMove);
        check(neutralDefender.getCurrentHp() == defenderHpBefore,
                "Damage calculation must not change the defender's HP.");
        check(fireMove.getCurrentPp() == movePpBefore,
                "Damage calculation must not consume PP.");

        Move unownedMove = createMove("Unowned Move", PokemonType.FIRE, 40);
        checkDamageRejected(attacker, neutralDefender, unownedMove,
                "A move not owned by the attacker should be rejected.");
        checkDamageRejected(null, neutralDefender, fireMove,
                "A null attacker should be rejected.");
        checkDamageRejected(attacker, null, fireMove,
                "A null defender should be rejected.");
        checkDamageRejected(attacker, neutralDefender, null,
                "A null move should be rejected.");
    }

    private static Move createMove(String name, PokemonType type, int power) {
        return new Move(name, type, power, 100, 0, 10);
    }

    private static Pokemon createPokemon(String name, PokemonType type,
            int attack, int defense, Move move) {
        return new Pokemon(name, type, 100, attack, defense, 50,
                new Move[] {move});
    }

    private static void checkTypeRejected(PokemonType attackingType,
            PokemonType defendingType, String failureMessage) {
        boolean rejected = false;
        try {
            BattleRules.getTypeMultiplier(attackingType, defendingType);
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void checkAccuracyRejected(Move move, int roll,
            String failureMessage) {
        boolean rejected = false;
        try {
            BattleRules.doesMoveHit(move, roll);
        } catch (IllegalArgumentException exception) {
            rejected = true;
        }
        check(rejected, failureMessage);
    }

    private static void checkDamageRejected(Pokemon attacker,
            Pokemon defender, Move move, String failureMessage) {
        boolean rejected = false;
        try {
            BattleRules.calculateDamage(attacker, defender, move);
        } catch (IllegalArgumentException exception) {
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
}
