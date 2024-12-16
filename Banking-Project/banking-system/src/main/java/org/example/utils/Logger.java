package org.example.utils;

public class Logger {
    public static void error(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_RED));
    }

    public static void printMainMenu(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_GREEN));
    }

    public static void printCustomerMenu(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_PURPLE));
    }

    public static void printSubMenu(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_CYAN));
    }

    public static void warning(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_YELLOW));
    }

    public static void printHint(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_CYAN));
    }

    public static void printStartEnd(String message) {
        System.out.println(Colors.colorize(message, Colors.ANSI_BOLD_BLUE));
    }
}
