package br.com.saveeditor.brasfoot.util;

/**
 * Helper class for console output formatting (tables, colors).
 */
public class ConsoleHelper {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    public static final String ANSI_CYAN = "\u001B[36m";


    public static String success(String msg) {
        return ANSI_GREEN + "✔ " + msg + ANSI_RESET;
    }

    public static String error(String msg) {
        return ANSI_RED + "✖ " + msg + ANSI_RESET;
    }

    public static String info(String msg) {
        return ANSI_CYAN + "ℹ " + msg + ANSI_RESET;
    }


}
