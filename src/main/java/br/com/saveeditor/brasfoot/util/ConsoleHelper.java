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

        StringBuilder sb = new StringBuilder();

        if (obj instanceof Collection) {
            return renderCollectionTable((Collection<?>) obj);
        } else if (obj.getClass().isArray()) {
            // Convert array to list for simplicity
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
        sb.append(ANSI_CYAN).append("╔════════════════════════════════╦════════════════════════════════╗\n")
                .append(ANSI_RESET);
        sb.append(ANSI_CYAN).append("║ Field                          ║ Value                          ║\n")
                .append(ANSI_RESET);
        sb.append(ANSI_CYAN).append("╠════════════════════════════════╬════════════════════════════════╣\n")
                .append(ANSI_RESET);

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object val = field.get(obj);
                String valStr = previewValue(val);
                sb.append(String.format("║ %-30s ║ %-30s ║\n",
                        truncate(field.getName(), 30),
                        truncate(valStr, 30)));
            } catch (Exception e) {
                // ignore
            }
        }
        sb.append(ANSI_CYAN).append("╚════════════════════════════════╩════════════════════════════════╝\n")
                .append(ANSI_RESET);
        return sb.toString();
    }

    private static String renderCollectionTable(Collection<?> col) {
        StringBuilder sb = new StringBuilder();
        sb.append(info("Collection Size: " + col.size())).append("\n");
        sb.append(ANSI_CYAN).append("╔══════╦════════════════════════════════════════════════════╗\n")
                .append(ANSI_RESET);
        sb.append(ANSI_CYAN).append("║ ID   ║ Value                                              ║\n")
                .append(ANSI_RESET);
        sb.append(ANSI_CYAN).append("╠══════╬════════════════════════════════════════════════════╣\n")
                .append(ANSI_RESET);

        int i = 0;
        for (Object item : col) {
            if (i >= 50) {
                sb.append(String.format("║ %-4s ║ ... (%d more items)                            ║\n",
                        "...", col.size() - 50));
                break;
            }
            sb.append(String.format("║ %-4d ║ %-50s ║\n", i, truncate(previewValue(item), 50)));
            i++;
        }
        sb.append(ANSI_CYAN).append("╚══════╩════════════════════════════════════════════════════╝\n")
                .append(ANSI_RESET);
        return sb.toString();
    }

    private static String truncate(String s, int len) {
        if (s == null)
            return "null";
        if (s.length() > len)
            return s.substring(0, len - 3) + "...";
        return s;
    }

    private static String previewValue(Object val) {
        if (val == null)
            return "null";
        if (ReflectionUtils.isComplexObject(val)) {
            try {
                // Try to find name 'dm' (common in Brasfoot)
                Field f = val.getClass().getDeclaredField("dm");
                f.setAccessible(true);
                Object name = f.get(val);
                if (name != null)
                    return val.getClass().getSimpleName() + " (" + name + ")";
            } catch (Exception e) {
            }
            return val.getClass().getSimpleName();
        }
        return val.toString();
    }
}
