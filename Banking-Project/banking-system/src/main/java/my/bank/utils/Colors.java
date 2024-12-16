package my.bank.utils;

public class Colors {
    // Reset
    public static final String ANSI_RESET = "\u001B[0m";

    // Regular Colors
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    // Bold Colors
    public static final String ANSI_BOLD_BLACK = "\u001B[1;30m";
    public static final String ANSI_BOLD_RED = "\u001B[1;31m";
    public static final String ANSI_BOLD_GREEN = "\u001B[1;32m";
    public static final String ANSI_BOLD_YELLOW = "\u001B[1;33m";
    public static final String ANSI_BOLD_BLUE = "\u001B[1;34m";
    public static final String ANSI_BOLD_PURPLE = "\u001B[1;35m";
    public static final String ANSI_BOLD_CYAN = "\u001B[1;36m";
    public static final String ANSI_BOLD_WHITE = "\u001B[1;37m";

    public static String colorize(String message, String color) {
        return color + message + ANSI_RESET;
    }
}
