public final class AnsiTheme {
    private static final String ESCAPE = "\u001B[";

    // Project palette from AGENTS.md.
    private static final String CORAL_RED = foreground(242, 85, 99);
    private static final String BERRY_RED = foreground(201, 60, 84);
    private static final String CHERRY_WINE = foreground(187, 58, 77);
    private static final String CREAM_PEACH = foreground(251, 210, 176);
    private static final String DUSTY_ROSE = foreground(229, 165, 146);
    private static final String WARM_ROSE = foreground(212, 135, 123);
    private static final String SOFT_WHITE = foreground(255, 244, 236);
    private static final String WARM_GRAY = foreground(207, 194, 188);
    private static final String BOLD = ESCAPE + "1m";
    private static final String RESET = ESCAPE + "0m";

    private final boolean colorsEnabled;

    public AnsiTheme(boolean colorsEnabled) {
        this.colorsEnabled = colorsEnabled;
    }

    public String coral(String text) {
        return style(text, CORAL_RED);
    }

    public String berry(String text) {
        return style(text, BERRY_RED);
    }

    public String cherry(String text) {
        return style(text, CHERRY_WINE);
    }

    public String cream(String text) {
        return style(text, CREAM_PEACH);
    }

    public String dustyRose(String text) {
        return style(text, DUSTY_ROSE);
    }

    public String warmRose(String text) {
        return style(text, WARM_ROSE);
    }

    public String white(String text) {
        return style(text, SOFT_WHITE);
    }

    public String gray(String text) {
        return style(text, WARM_GRAY);
    }

    public String boldCream(String text) {
        return style(text, BOLD + CREAM_PEACH);
    }

    public String boldWhite(String text) {
        return style(text, BOLD + SOFT_WHITE);
    }

    public String reset() {
        return colorsEnabled ? RESET : "";
    }

    public boolean isColorsEnabled() {
        return colorsEnabled;
    }

    private String style(String text, String codes) {
        if (!colorsEnabled) {
            return text;
        }
        return codes + text + RESET;
    }

    private static String foreground(int red, int green, int blue) {
        return ESCAPE + "38;2;" + red + ";" + green + ";" + blue + "m";
    }
}
