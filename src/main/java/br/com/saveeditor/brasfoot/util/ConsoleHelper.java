package br.com.saveeditor.brasfoot.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Helper class for console output formatting (tables, colors).
 */
public class ConsoleHelper {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001B[1m";

    public static String success(String msg) {
        return ANSI_GREEN + "✔ " + msg + ANSI_RESET;
    }

    public static String error(String msg) {
        return ANSI_RED + "✖ " + msg + ANSI_RESET;
    }

    public static String info(String msg) {
        return ANSI_CYAN + "ℹ " + msg + ANSI_RESET;
    }

    public static String warning(String msg) {
        return ANSI_YELLOW + "⚠ " + msg + ANSI_RESET;
    }

    /**
     * Renders a simple ASCII table for object fields or collection items.
     */
    public static String renderTable(Object obj) {
        if (obj == null)
            return "null";

        if (obj instanceof Collection) {
            return renderCollectionTable((Collection<?>) obj);
        } else if (obj.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            int len = Array.getLength(obj);
            for (int i = 0; i < len; i++)
                list.add(Array.get(obj, i));
            return renderCollectionTable(list);
        } else {
            return renderObjectTable(obj);
        }
    }

    private static String renderObjectTable(Object obj) {
        StringBuilder sb = new StringBuilder();
        String border = ANSI_CYAN + "╔" + "═".repeat(32) + "╦" + "═".repeat(40) + "╗" + ANSI_RESET + "\n";
        String separator = ANSI_CYAN + "╠" + "═".repeat(32) + "╬" + "═".repeat(40) + "╣" + ANSI_RESET + "\n";
        String footer = ANSI_CYAN + "╚" + "═".repeat(32) + "╩" + "═".repeat(40) + "╝" + ANSI_RESET + "\n";

        sb.append(border);
        sb.append(ANSI_CYAN).append(String.format("║ %-30s ║ %-38s ║", "Field", "Value")).append(ANSI_RESET)
                .append("\n");
        sb.append(separator);

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                String valStr = previewValue(val);
                sb.append(String.format("║ %-30s ║ %-38s ║\n",
                        truncate(field.getName(), 30),
                        truncate(valStr, 38)));
            } catch (Exception e) {
            }
        }
        sb.append(footer);
        return sb.toString();
    }

    private static String renderCollectionTable(Collection<?> col) {
        StringBuilder sb = new StringBuilder();
        sb.append(info("Collection Size: " + col.size())).append("\n");

        String border = ANSI_CYAN + "╔" + "═".repeat(6) + "╦" + "═".repeat(60) + "╗" + ANSI_RESET + "\n";
        String separator = ANSI_CYAN + "╠" + "═".repeat(6) + "╬" + "═".repeat(60) + "╣" + ANSI_RESET + "\n";
        String footer = ANSI_CYAN + "╚" + "═".repeat(6) + "╩" + "═".repeat(60) + "╝" + ANSI_RESET + "\n";

        sb.append(border);
        sb.append(ANSI_CYAN).append(String.format("║ %-4s ║ %-58s ║", "Idx", "Value")).append(ANSI_RESET).append("\n");
        sb.append(separator);

        int i = 0;
        for (Object item : col) {
            if (i >= 50) {
                sb.append(String.format("║ %-4s ║ %-58s ║\n", "...", "... (" + (col.size() - 50) + " more)"));
                break;
            }
            sb.append(String.format("║ %-4d ║ %-58s ║\n", i, truncate(previewValue(item), 58)));
            i++;
        }
        sb.append(footer);
        return sb.toString();
    }

    private static String truncate(String s, int len) {
        if (s == null)
            return "null";
        // Remove ANSI codes for length calculation if needed, but simple truncate for
        // now
        if (s.length() > len)
            return s.substring(0, len - 3) + "...";
        return s;
    }

    private static String previewValue(Object val) {
        if (val == null)
            return "null";

        // Handle common Brasfoot types
        if (val.getClass().isArray()) {
            return "Array[" + Array.getLength(val) + "]";
        }

        if (ReflectionUtils.isComplexObject(val)) {
            try {
                // Try to resolve 'dm' (Team name?) or 'nome'
                Field[] fields = val.getClass().getDeclaredFields();
                for (Field f : fields) {
                    if (f.getName().equals("dm") || f.getName().toLowerCase().contains("name")
                            || f.getName().equals("nome")) {
                        f.setAccessible(true);
                        Object name = f.get(val);
                        if (name != null)
                            return val.getClass().getSimpleName() + " (" + name + ")";
                    }
                }
            } catch (Exception e) {
            }
            return val.getClass().getSimpleName();
        }
        return val.toString();
    }

    // Shared scanner to avoid resource leaks
    private static final java.util.Scanner SHARED_SCANNER = new java.util.Scanner(System.in);

    public static String ask(String prompt) {
        System.out.print(ANSI_YELLOW + prompt + " " + ANSI_RESET);
        return SHARED_SCANNER.nextLine();
    }

    public static boolean confirm(String prompt, boolean force) {
        if (force)
            return true;
        System.out.print(ANSI_YELLOW + prompt + " [y/N] " + ANSI_RESET);
        String input = SHARED_SCANNER.nextLine().trim();
        return input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes");
    }
}
