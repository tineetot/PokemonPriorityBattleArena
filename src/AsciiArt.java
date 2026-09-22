public final class AsciiArt {
    private static final String[] POKEMON_LOGO = {
        " ____   ___  _  __ _____ __  __  ___  _   _ ",
        "|  _ \\ / _ \\| |/ /| ____|  \\/  |/ _ \\| \\ | |",
        "| |_) | | | | ' / |  _| | |\\/| | | | |  \\| |",
        "|  __/| |_| | . \\ | |___| |  | | |_| | |\\  |",
        "|_|    \\___/|_|\\_\\|_____|_|  |_|\\___/|_| \\_|"
    };

    private static final String[] BATTLE_SCENE = {
        "      .--.                                        .--.      ",
        "   .-(____)-.                                  .-(____)-.   ",
        "      /\\             *     .----.     *             /\\      ",
        "     /  \\                 /      \\                 /  \\     ",
        "    /____\\               |---()---|               /____\\    ",
        "      ||                  \\      /                  ||      ",
        "  ___/||\\___              '----'              ___/||\\___  ",
        "^^^..^^^..^^^..^^^..^^^..^^^....^^^..^^^..^^^..^^^..^^^..^^^"
    };

    private static final String[] D_PAD = {
        "   [^]   ",
        "[<][+][>]",
        "   [v]   "
    };

    private static final String[] SPEAKER = {
        ". . . .",
        " . . . ."
    };

    private AsciiArt() {
        // Utility class; no objects are needed.
    }

    public static String[] pokemonLogo() {
        return POKEMON_LOGO.clone();
    }

    public static String[] battleScene() {
        return BATTLE_SCENE.clone();
    }

    public static String[] dPad() {
        return D_PAD.clone();
    }

    public static String[] speaker() {
        return SPEAKER.clone();
    }
}
