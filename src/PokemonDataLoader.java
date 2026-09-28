import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/** Loads the small, controlled CSV dataset into Pokemon and Move objects. */
public final class PokemonDataLoader {
    private static final String MOVE_HEADER =
            "id,name,type,power,accuracy,priority,max_pp";
    private static final String POKEMON_HEADER =
            "id,name,type,hp,attack,defense,speed,move_1,move_2,move_3,move_4";

    public Pokemon[] load(String pokemonCsvPath, String movesCsvPath) throws IOException {
        validatePath(pokemonCsvPath, "Pokemon CSV");
        validatePath(movesCsvPath, "Move CSV");

        MoveDefinition[] moveDefinitions = loadMoveDefinitions(movesCsvPath);
        return loadPokemon(pokemonCsvPath, moveDefinitions);
    }

    public Pokemon[] loadDefaultDataset() throws IOException {
        return load("data/pokemon.csv", "data/moves.csv");
    }

    private MoveDefinition[] loadMoveDefinitions(String filename) throws IOException {
        int rowCount = countDataRows(filename, MOVE_HEADER, "move");
        MoveDefinition[] definitions = new MoveDefinition[rowCount];

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            int lineNumber = 1;
            int definitionIndex = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitRow(line);
                requireColumnCount(columns, 7, filename, lineNumber);
                String id = columns[0];
                rejectDuplicateMoveId(definitions, definitionIndex, id,
                        filename, lineNumber);

                try {
                    definitions[definitionIndex] = new MoveDefinition(
                            id,
                            columns[1],
                            parseType(columns[2], filename, lineNumber),
                            parseInteger(columns[3], "power", filename, lineNumber),
                            parseInteger(columns[4], "accuracy", filename, lineNumber),
                            parseInteger(columns[5], "priority", filename, lineNumber),
                            parseInteger(columns[6], "max_pp", filename, lineNumber));
                } catch (IllegalArgumentException exception) {
                    if (isContextualMessage(exception.getMessage(), filename, lineNumber)) {
                        throw exception;
                    }
                    throw invalidRow(filename, lineNumber, exception.getMessage(), exception);
                }
                definitionIndex++;
            }
        } catch (IOException exception) {
            throw fileReadFailure(filename, exception);
        }

        return definitions;
    }

    private Pokemon[] loadPokemon(String filename, MoveDefinition[] definitions)
            throws IOException {
        int rowCount = countDataRows(filename, POKEMON_HEADER, "Pokemon");
        Pokemon[] pokemon = new Pokemon[rowCount];
        String[] pokemonIds = new String[rowCount];

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            int lineNumber = 1;
            int pokemonIndex = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitRow(line);
                requireColumnCount(columns, 11, filename, lineNumber);
                rejectDuplicatePokemonId(pokemonIds, pokemonIndex, columns[0],
                        filename, lineNumber);

                Move[] moves = new Move[4];
                for (int moveIndex = 0; moveIndex < moves.length; moveIndex++) {
                    MoveDefinition definition = findMoveDefinition(
                            definitions, columns[7 + moveIndex]);
                    if (definition == null) {
                        throw invalidRow(filename, lineNumber,
                                "Move ID '" + columns[7 + moveIndex]
                                        + "' does not exist in the move dataset.",
                                null);
                    }
                    // A fresh object gives every Pokemon independent current PP.
                    moves[moveIndex] = definition.createMove();
                }

                try {
                    pokemon[pokemonIndex] = new Pokemon(
                            columns[1],
                            parseType(columns[2], filename, lineNumber),
                            parseInteger(columns[3], "hp", filename, lineNumber),
                            parseInteger(columns[4], "attack", filename, lineNumber),
                            parseInteger(columns[5], "defense", filename, lineNumber),
                            parseInteger(columns[6], "speed", filename, lineNumber),
                            moves);
                } catch (IllegalArgumentException exception) {
                    if (isContextualMessage(exception.getMessage(), filename, lineNumber)) {
                        throw exception;
                    }
                    throw invalidRow(filename, lineNumber, exception.getMessage(), exception);
                }

                pokemonIds[pokemonIndex] = columns[0];
                pokemonIndex++;
            }
        } catch (IOException exception) {
            throw fileReadFailure(filename, exception);
        }

        return pokemon;
    }

    private int countDataRows(String filename, String expectedHeader, String dataName)
            throws IOException {
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String header = reader.readLine();
            if (!expectedHeader.equals(header)) {
                throw new IllegalArgumentException(
                        "Invalid header in '" + filename + "' at line 1. Expected: "
                                + expectedHeader);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    count++;
                }
            }
        } catch (IOException exception) {
            throw fileReadFailure(filename, exception);
        }

        if (count == 0) {
            throw new IllegalArgumentException(
                    "The " + dataName + " dataset in '" + filename + "' is empty.");
        }
        return count;
    }

    private String[] splitRow(String line) {
        // This project controls the CSV files, which do not contain quoted commas.
        String[] columns = line.split(",", -1);
        for (int index = 0; index < columns.length; index++) {
            columns[index] = columns[index].trim();
        }
        return columns;
    }

    private PokemonType parseType(String value, String filename, int lineNumber) {
        try {
            return PokemonType.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw invalidRow(filename, lineNumber,
                    "Invalid Pokemon type '" + value + "'.", exception);
        }
    }

    private int parseInteger(String value, String fieldName,
            String filename, int lineNumber) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw invalidRow(filename, lineNumber,
                    "Invalid integer for " + fieldName + ": '" + value + "'.",
                    exception);
        }
    }

    private MoveDefinition findMoveDefinition(MoveDefinition[] definitions, String id) {
        for (MoveDefinition definition : definitions) {
            if (definition.getId().equals(id)) {
                return definition;
            }
        }
        return null;
    }

    private void rejectDuplicateMoveId(MoveDefinition[] definitions, int usedLength,
            String id, String filename, int lineNumber) {
        for (int index = 0; index < usedLength; index++) {
            if (definitions[index].getId().equals(id)) {
                throw invalidRow(filename, lineNumber,
                        "Duplicate move ID '" + id + "'.", null);
            }
        }
    }

    private void rejectDuplicatePokemonId(String[] ids, int usedLength,
            String id, String filename, int lineNumber) {
        if (id == null || id.trim().isEmpty()) {
            throw invalidRow(filename, lineNumber,
                    "Pokemon ID must not be blank.", null);
        }
        for (int index = 0; index < usedLength; index++) {
            if (ids[index].equals(id)) {
                throw invalidRow(filename, lineNumber,
                        "Duplicate Pokemon ID '" + id + "'.", null);
            }
        }
    }

    private void requireColumnCount(String[] columns, int expected,
            String filename, int lineNumber) {
        if (columns.length != expected) {
            throw invalidRow(filename, lineNumber,
                    "Expected " + expected + " columns but found " + columns.length + ".",
                    null);
        }
    }

    private void validatePath(String path, String description) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException(description + " path must not be null or blank.");
        }
    }

    private IllegalArgumentException invalidRow(String filename, int lineNumber,
            String detail, Throwable cause) {
        String message = "Invalid data in '" + filename + "' at line "
                + lineNumber + ": " + detail;
        return new IllegalArgumentException(message, cause);
    }

    private boolean isContextualMessage(String message, String filename, int lineNumber) {
        return message != null && message.startsWith(
                "Invalid data in '" + filename + "' at line " + lineNumber + ":");
    }

    private IOException fileReadFailure(String filename, IOException cause) {
        return new IOException("Could not read CSV file '" + filename + "'.", cause);
    }
}
