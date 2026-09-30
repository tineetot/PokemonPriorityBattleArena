import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class PokemonDataLoaderTest {
    private static int checksPassed = 0;

    private PokemonDataLoaderTest() {
        // Test class; no objects are needed.
    }

    public static void main(String[] args) throws IOException {
        PokemonDataLoader loader = new PokemonDataLoader();
        Pokemon[] pokemon = loader.loadDefaultDataset();

        check(pokemon != null, "The default dataset should load successfully.");
        check(pokemon.length == 8, "The dataset should contain exactly 8 Pokemon.");

        boolean allPokemonPresent = true;
        boolean allHaveFourMoves = true;
        boolean allMovesHaveFullPp = true;
        for (Pokemon currentPokemon : pokemon) {
            if (currentPokemon == null) {
                allPokemonPresent = false;
                continue;
            }
            if (currentPokemon.getMoves().length != 4) {
                allHaveFourMoves = false;
            }
            for (Move move : currentPokemon.getMoves()) {
                if (move == null || move.getCurrentPp() != move.getMaxPp()) {
                    allMovesHaveFullPp = false;
                }
            }
        }
        check(allPokemonPresent, "No loaded Pokemon should be null.");
        check(allHaveFourMoves, "Every Pokemon should have exactly four moves.");
        check(allMovesHaveFullPp, "Every move should begin with full PP.");

        Pokemon pikachu = findPokemon(pokemon, "Pikachu");
        Pokemon vulpix = findPokemon(pokemon, "Vulpix");
        check(pikachu != null, "Pikachu should be present in the dataset.");
        check(vulpix != null, "Vulpix should be present in the dataset.");
        check(pikachu.getType() == PokemonType.ELECTRIC,
                "Pikachu should have the ELECTRIC type.");
        check(pikachu.getMaxHp() == 35, "Pikachu should have 35 HP.");
        check(pikachu.getAttack() == 55, "Pikachu should have 55 Attack.");
        check(pikachu.getDefense() == 40, "Pikachu should have 40 Defense.");
        check(pikachu.getSpeed() == 90, "Pikachu should have 90 Speed.");
        check(pikachu.getMoves().length == 4, "Pikachu should have exactly four moves.");

        Move pikachuQuickAttack = findMove(pikachu, "Quick Attack");
        Move vulpixQuickAttack = findMove(vulpix, "Quick Attack");
        check(pikachuQuickAttack != null, "Pikachu should know Quick Attack.");
        check(vulpixQuickAttack != null, "Vulpix should know Quick Attack.");
        check(pikachuQuickAttack != vulpixQuickAttack,
                "Different Pokemon must not share the same Move object.");

        int vulpixPpBeforeUse = vulpixQuickAttack.getCurrentPp();
        pikachuQuickAttack.usePp();
        check(vulpixQuickAttack.getCurrentPp() == vulpixPpBeforeUse,
                "Using Pikachu's move must not change Vulpix's PP.");

        Move discharge = findMove(pikachu, "Discharge");
        check(discharge != null, "Discharge should load for Pikachu.");
        check(discharge.getPower() == 80,
                "Discharge should load with its fixed power of 80.");

        Pokemon[] secondLoad = loader.loadDefaultDataset();
        Pokemon secondPikachu = findPokemon(secondLoad, "Pikachu");
        Move secondQuickAttack = findMove(secondPikachu, "Quick Attack");
        check(secondPikachu != pikachu,
                "Loading twice should create fresh Pokemon objects.");
        check(secondQuickAttack != pikachuQuickAttack,
                "Loading twice should create fresh Move objects.");
        check(secondQuickAttack.getCurrentPp() == secondQuickAttack.getMaxPp(),
                "Moves from a second load should begin with full PP.");

        testMissingMoveReference(loader);

        System.out.println("All " + checksPassed
                + " PokemonDataLoader checks passed.");
    }

    private static Pokemon findPokemon(Pokemon[] pokemon, String name) {
        for (Pokemon currentPokemon : pokemon) {
            if (currentPokemon != null && currentPokemon.getName().equals(name)) {
                return currentPokemon;
            }
        }
        return null;
    }

    private static Move findMove(Pokemon pokemon, String name) {
        if (pokemon == null) {
            return null;
        }
        for (Move move : pokemon.getMoves()) {
            if (move.getName().equals(name)) {
                return move;
            }
        }
        return null;
    }

    private static void testMissingMoveReference(PokemonDataLoader loader)
            throws IOException {
        String moveData = "id,name,type,power,accuracy,priority,max_pp\n"
                + "tackle,Tackle,NORMAL,40,100,0,35\n";
        String pokemonData =
                "id,name,type,hp,attack,defense,speed,move_1,move_2,move_3,move_4\n"
                + "testmon,Testmon,NORMAL,50,50,50,50,"
                + "tackle,tackle,tackle,missing-move\n";

        File moveFile = createTemporaryCsv("moves-test", moveData);
        File pokemonFile = createTemporaryCsv("pokemon-test", pokemonData);
        boolean missingReferenceRejected = false;

        try {
            loader.load(pokemonFile.getAbsolutePath(), moveFile.getAbsolutePath());
        } catch (IllegalArgumentException exception) {
            String message = exception.getMessage();
            missingReferenceRejected = message.contains(pokemonFile.getAbsolutePath())
                    && message.contains("line 2")
                    && message.contains("missing-move");
        } finally {
            pokemonFile.delete();
            moveFile.delete();
        }

        check(missingReferenceRejected,
                "A missing move reference should report its file, line, and move ID.");
    }

    private static File createTemporaryCsv(String prefix, String content)
            throws IOException {
        File file = File.createTempFile(prefix, ".csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private static void check(boolean condition, String failureMessage) {
        if (!condition) {
            throw new AssertionError(failureMessage);
        }
        checksPassed++;
    }
}
