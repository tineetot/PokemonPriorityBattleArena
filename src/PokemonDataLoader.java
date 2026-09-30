// Reads and validates the two CSV files, connects move IDs to Pokémon, 
// and returns a usable Pokemon[].

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public final class PokemonDataLoader {
    private static final String MOVE_HEADER =
            "id,name,type,power,accuracy,priority,max_pp";
    private static final String POKEMON_HEADER =
            "id,name,type,hp,attack,defense,speed,move_1,move_2,move_3,move_4";

    public Pokemon[] load(String pokemonCsvPath, String movesCsvPath) throws IOException {
        validatePath(pokemonCsvPath, "Pokemon CSV");
        validatePath(movesCsvPath, "Move CSV");

        MoveTemplate[] moveTemplates = loadMoveTemplates(movesCsvPath);
        return loadPokemon(pokemonCsvPath, moveTemplates);
    }

    public Pokemon[] loadDefaultDataset() throws IOException {
        return load("data/pokemon.csv", "data/moves.csv");
    }

    private MoveTemplate[] loadMoveTemplates(String filename) throws IOException {
        int rowCount = countDataRows(filename, MOVE_HEADER, "move");
        MoveTemplate[] templates = new MoveTemplate[rowCount];

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            reader.readLine();
            String line;
            int lineNumber = 1;
            int templateIndex = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] columns = splitRow(line);
                requireColumnCount(columns, 7, filename, lineNumber);
                String id = columns[0];
                rejectDuplicateMoveId(templates, templateIndex, id,
                        filename, lineNumber);

                try {
                    templates[templateIndex] = new MoveTemplate(
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
                templateIndex++;
            }
        } catch (IOException exception) {
            throw fileReadFailure(filename, exception);
        }

        return templates;
    }

    private Pokemon[] loadPokemon(String filename, MoveTemplate[] templates)
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
                    MoveTemplate template = findMoveTemplate(
                            templates, columns[7 + moveIndex]);
                    if (template == null) {
                        throw invalidRow(filename, lineNumber,
                                "Move ID '" + columns[7 + moveIndex]
                                        + "' does not exist in the move dataset.",
                                null);
                    }
                    // A fresh object gives every Pokemon independent current PP.
                    moves[moveIndex] = template.createMove();
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

    private MoveTemplate findMoveTemplate(MoveTemplate[] templates, String id) {
        for (MoveTemplate template : templates) {
            if (template.id.equals(id)) {
                return template;
            }
        }
        return null;
    }

    private void rejectDuplicateMoveId(MoveTemplate[] templates, int usedLength,
            String id, String filename, int lineNumber) {
        for (int index = 0; index < usedLength; index++) {
            if (templates[index].id.equals(id)) {
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

    /** Stores one move row and creates independent Move objects from it. */
    private static final class MoveTemplate {
        private final String id;
        private final String name;
        private final PokemonType type;
        private final int power;
        private final int accuracy;
        private final int priority;
        private final int maxPp;

        private MoveTemplate(String id, String name, PokemonType type, int power,
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
                throw new IllegalArgumentException(
                        "Move accuracy must be between 1 and 100.");
            }
            if (maxPp <= 0) {
                throw new IllegalArgumentException(
                        "Move maximum PP must be greater than zero.");
            }

            this.id = id;
            this.name = name;
            this.type = type;
            this.power = power;
            this.accuracy = accuracy;
            this.priority = priority;
            this.maxPp = maxPp;
        }

        private Move createMove() {
            return new Move(name, type, power, accuracy, priority, maxPp);
        }
    }
}
