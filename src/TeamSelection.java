/** Stores one player's three unique Pokemon choices. */
public final class TeamSelection {
    public static final int TEAM_SIZE = 3;

    private final Pokemon[] availablePokemon;
    private final Pokemon[] selectedTeam;
    private int selectedCount;

    public TeamSelection(Pokemon[] availablePokemon) {
        if (availablePokemon == null) {
            throw new IllegalArgumentException("Available Pokemon must not be null.");
        }
        if (availablePokemon.length < TEAM_SIZE) {
            throw new IllegalArgumentException(
                    "At least three available Pokemon are required.");
        }
        for (int index = 0; index < availablePokemon.length; index++) {
            if (availablePokemon[index] == null) {
                throw new IllegalArgumentException(
                        "Available Pokemon at index " + index + " must not be null.");
            }
        }

        // Protect the available roster from changes made outside this class.
        this.availablePokemon = availablePokemon.clone();
        this.selectedTeam = new Pokemon[TEAM_SIZE];
        this.selectedCount = 0;
    }

    public boolean isFull() {
        return selectedCount == TEAM_SIZE;
    }

    public boolean isSelected(Pokemon pokemon) {
        if (pokemon == null) {
            return false;
        }
        for (int index = 0; index < selectedCount; index++) {
            if (selectedTeam[index] == pokemon) {
                return true;
            }
        }
        return false;
    }

    public boolean isSelected(int datasetIndex) {
        validateDatasetIndex(datasetIndex);
        return isSelected(availablePokemon[datasetIndex]);
    }

    public void addPokemon(int datasetIndex) {
        validateDatasetIndex(datasetIndex);
        Pokemon pokemon = availablePokemon[datasetIndex];

        if (isSelected(pokemon)) {
            throw new IllegalArgumentException(
                    pokemon.getName() + " has already been selected.");
        }
        if (isFull()) {
            throw new IllegalStateException(
                    "The team already contains three Pokemon.");
        }

        selectedTeam[selectedCount] = pokemon;
        selectedCount++;
    }

    public int getSelectedCount() {
        return selectedCount;
    }

    public Pokemon[] getSelectedTeam() {
        return selectedTeam.clone();
    }

    private void validateDatasetIndex(int datasetIndex) {
        if (datasetIndex < 0 || datasetIndex >= availablePokemon.length) {
            throw new IndexOutOfBoundsException(
                    "Pokemon index must be between 0 and "
                            + (availablePokemon.length - 1) + ".");
        }
    }
}
