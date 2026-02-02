package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

@Service
public class DebugService {

    public List<String> searchRecursive(Object root, String value, int maxDepth) {
        if (root == null)
            return Collections.emptyList();

        List<String> results = new ArrayList<>();
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        search(root, value, 0, maxDepth, visited, "root", results);

        return results;
    }

    private void search(Object current, String valueStr, int depth, int maxDepth, Set<Object> visited, String path,
            List<String> results) {
        if (current == null || depth > maxDepth)
            return;

        // Prevent cycles and re-visiting same objects
        if (!visited.add(current))
            return;

        // Skip primitives and Strings (leaf nodes) from further traversal, but check
        // values
        if (isLeaf(current)) {
            // Check value directly
            if (checkMatch(current, valueStr)) {
                results.add(path + " = " + current);
            }
            return;
        }

        Class<?> clazz = current.getClass();

        // Arrays
        if (clazz.isArray()) {
            // Handle array traversal if needed, or simply skip for now to avoid complexity
            // in this version
            // For int[] etc it is hard to cast to Object[]
            return;
        }

        // Collections (List, Set)
        if (current instanceof Collection<?>) {
            int index = 0;
            for (Object item : (Collection<?>) current) {
                search(item, valueStr, depth + 1, maxDepth, visited, path + "[" + index + "]", results);
                index++;
            }
            return;
        }

        // Maps
        if (current instanceof Map<?, ?>) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) current).entrySet()) {
                search(entry.getValue(), valueStr, depth + 1, maxDepth, visited, path + "['" + entry.getKey() + "']",
                        results);
            }
            return;
        }

        // Standard Objects - Reflect Fields
        for (Field f : getAllFields(clazz)) {
            f.setAccessible(true);
            try {
                Object val = f.get(current);

                // 1. Check if the field value MATCHES
                if (val != null && isSimpleType(val.getClass())) {
                    if (checkMatch(val, valueStr)) {
                        results.add(ConsoleHelper.success("FOUND: " + path + "." + f.getName() + " ("
                                + f.getType().getSimpleName() + ") = " + val));
                    }
                }

                // 2. Recurse into complex objects
                if (val != null && !isSimpleType(val.getClass())) {
                    search(val, valueStr, depth + 1, maxDepth, visited, path + "." + f.getName(), results);
                }

            } catch (Exception ignored) {
            }
        }
    }

    private boolean checkMatch(Object val, String searchStr) {
        // String match
        if (val.toString().equals(searchStr))
            return true;

        // Number match
        try {
            long searchNum = Long.parseLong(searchStr);
            if (val instanceof Number) {
                long valNum = ((Number) val).longValue();
                if (valNum == searchNum)
                    return true;
            }
        } catch (NumberFormatException ignored) {
        }

        return false;
    }

    private boolean isLeaf(Object obj) {
        return obj instanceof String || obj instanceof Number || obj instanceof Boolean || obj instanceof Character;
    }

    private boolean isSimpleType(Class<?> type) {
        return type.isPrimitive() ||
                Number.class.isAssignableFrom(type) ||
                String.class.isAssignableFrom(type) ||
                Boolean.class.isAssignableFrom(type) ||
                Character.class.isAssignableFrom(type);
    }

    private List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null && type != Object.class) {
            Collections.addAll(fields, type.getDeclaredFields());
            type = type.getSuperclass();
        }
        return fields;
    }

    // --- Legacy Inspection Method (Optional to keep) ---
    // --- Export Logic ---

    public void exportSearchToFile(Object root, String arg) {
        String[] args = arg.split(";", 2);
        if (args.length < 2) {
            throw new IllegalArgumentException("Sintaxe: <arquivo.txt>; <termo>");
        }
        String fileName = args[0].trim();
        String term = args[1].trim().toLowerCase();

        if (!fileName.toLowerCase().endsWith(".txt")) {
            fileName += ".txt";
        }

        System.out.println(ConsoleHelper.info("Exporting search for '" + term + "' to " + fileName + "..."));

        try (FileWriter fw = new FileWriter(fileName); PrintWriter pw = new PrintWriter(fw)) {
            int[] counter = { 0 };
            Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
            exportRecursive(pw, root, term, "root", visited, counter);

            if (counter[0] > 0) {
                System.out.println(ConsoleHelper.success("Exported " + counter[0] + " matches to " + fileName));
            } else {
                System.out.println(ConsoleHelper.warning("No matches found to export."));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing file: " + e.getMessage());
        }
    }

    private void exportRecursive(PrintWriter pw, Object obj, String term, String path, Set<Object> visited,
            int[] counter) {
        if (obj == null || !visited.add(obj))
            return;

        // Check if object matches
        boolean objectMatches = false;
        try {
            if (obj.toString().toLowerCase().contains(term))
                objectMatches = true;
        } catch (Exception ignored) {
        }

        List<Field> fieldMatches = new ArrayList<>();
        if (!isSimpleType(obj.getClass())) {
            for (Field f : getAllFields(obj.getClass())) {
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    if (val != null && val.toString().toLowerCase().contains(term)) {
                        fieldMatches.add(f);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        // Print match if found
        if (objectMatches || !fieldMatches.isEmpty()) {
            pw.println("=".repeat(80));
            pw.println("MATCH FOUND @ " + path);
            pw.println("Class: " + obj.getClass().getName());
            if (objectMatches)
                pw.println("Value: " + obj.toString());
            for (Field f : fieldMatches) {
                try {
                    pw.println("Field '" + f.getName() + "': " + f.get(obj));
                } catch (Exception ignored) {
                }
            }
            pw.println("=".repeat(80) + "\n");
            counter[0]++;
        }

        // Recurse
        if (isSimpleType(obj.getClass()))
            return;

        if (obj instanceof Collection) {
            int i = 0;
            for (Object item : (Collection<?>) obj) {
                exportRecursive(pw, item, term, path + "[" + i++ + "]", visited, counter);
            }
        } else if (obj.getClass().isArray()) {
            // Arrays skipped
        } else {
            for (Field f : getAllFields(obj.getClass())) {
                f.setAccessible(true);
                try {
                    Object val = f.get(obj);
                    exportRecursive(pw, val, term, path + "." + f.getName(), visited, counter);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
