package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.model.Preset;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class PresetService {

    private static final String PRESETS_DIR = "presets";
    private final Gson gson;

    public PresetService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDefaultPresets();
    }

    private void initializeDefaultPresets() {
        File folder = new File(PRESETS_DIR);
        if (!folder.exists()) {
            System.out.println(ConsoleHelper.info("Initializing 'presets' directory with default files..."));
            if (folder.mkdirs()) {
                try {
                    // Read index.txt from classpath
                    java.io.InputStream indexStream = getClass().getResourceAsStream("/presets/index.txt");
                    if (indexStream != null) {
                        try (java.util.Scanner scanner = new java.util.Scanner(indexStream, "UTF-8")) {
                            while (scanner.hasNextLine()) {
                                String filename = scanner.nextLine().trim();
                                if (!filename.isEmpty()) {
                                    extractResource("/presets/" + filename, new File(folder, filename));
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Failed to extract presets: " + e.getMessage());
                }
            }
        }
    }

    private void extractResource(String resourcePath, File destination) {
        try (java.io.InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null)
                return;
            try (java.io.FileOutputStream out = new java.io.FileOutputStream(destination)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            System.err.println("Error extracting " + resourcePath + ": " + e.getMessage());
        }
    }

    public List<String> listPresets() {
        File folder = new File(PRESETS_DIR);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        List<String> names = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                names.add(f.getName().replace(".json", ""));
            }
        }
        return names;
    }

    public Preset loadPreset(String name) {
        try (FileReader reader = new FileReader(PRESETS_DIR + "/" + name + ".json")) {
            return gson.fromJson(reader, Preset.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void savePreset(Preset preset) {
        try (FileWriter writer = new FileWriter(PRESETS_DIR + "/" + preset.getName() + ".json")) {
            gson.toJson(preset, writer);
            System.out.println(ConsoleHelper.success("Preset saved: " + preset.getName()));
        } catch (Exception e) {
            System.out.println(ConsoleHelper.error("Error saving preset: " + e.getMessage()));
        }
    }

    public void applyPreset(Object target, String presetName) {
        Preset preset = loadPreset(presetName);
        if (preset == null) {
            System.out.println(ConsoleHelper.error("Preset not found: " + presetName));
            return;
        }

        System.out.println(ConsoleHelper.info("Applying preset '" + presetName + "' (" + preset.getType() + ")..."));

        int appliedCount = 0;
        for (Map.Entry<String, Object> entry : preset.getAttributes().entrySet()) {
            String readableKey = entry.getKey();
            Object value = entry.getValue();

            // Special Handling (e.g. Stadium Sectors)
            if ("TEAM_STADIUM_SECTORS".equals(readableKey)) {
                try {
                    // Navigate to Stadium object
                    Object stadium = ReflectionUtils.getFieldValue(target, BrasfootConstants.TEAM_STADIUM);
                    if (stadium != null) {
                        try {
                            List<?> listValues = (List<?>) value; // GSON parses array as List
                            int[] sectorValues = new int[4];
                            for (int i = 0; i < 4; i++) {
                                if (i < listValues.size())
                                    sectorValues[i] = ((Number) listValues.get(i)).intValue();
                                else
                                    sectorValues[i] = 10000; // default
                            }

                            // Get array field (int[])
                            Field f = stadium.getClass().getDeclaredField(BrasfootConstants.STADIUM_SECTORS);
                            f.setAccessible(true);
                            Object sectorsArray = f.get(stadium);

                            if (sectorsArray != null && sectorsArray.getClass().isArray()) {
                                for (int i = 0; i < 4; i++) {
                                    java.lang.reflect.Array.setInt(sectorsArray, i, sectorValues[i]);
                                }
                                System.out.println(ConsoleHelper.success(
                                        "Updated stadium sectors to " + java.util.Arrays.toString(sectorValues)));
                                appliedCount++;
                            }
                        } catch (Exception e) {
                            System.out.println(
                                    ConsoleHelper.error("Failed to update stadium sectors: " + e.getMessage()));
                        }
                    }
                } catch (Exception e) {
                    System.out.println(ConsoleHelper.error("Failed to find stadium object: " + e.getMessage()));
                }
                continue; // Skip standard handling
            }

            // Resolve readable key to obfuscated field name
            String obfuscatedField = resolveField(readableKey);
            if (obfuscatedField != null) {
                try {
                    // Quick fix for Number types (JSON usually parses numbers as Double)
                    if (value instanceof Number) {
                        // We need to match the target field type to avoid IllegalArgumentException
                        Field f = target.getClass().getDeclaredField(obfuscatedField);
                        f.setAccessible(true);
                        if (f.getType() == int.class || f.getType() == Integer.class) {
                            value = ((Number) value).intValue();
                        } else if (f.getType() == long.class || f.getType() == Long.class) {
                            value = ((Number) value).longValue();
                        }
                    }

                    ReflectionUtils.setFieldValue(target, obfuscatedField, value);
                    appliedCount++;
                } catch (Exception e) {
                    // System.out.println("Field not found or mismatch: " + readableKey);
                }
            }
        }
        System.out.println(ConsoleHelper.success("Applied " + appliedCount + " attributes."));

        // Cascade logic: If it's a TEAM preset and has player_attributes, apply to all
        // players
        if ("TEAM".equals(preset.getType()) && !preset.getPlayer_attributes().isEmpty()) {
            try {
                List<?> players = (List<?>) ReflectionUtils.getFieldValue(target, BrasfootConstants.TEAM_PLAYERS);
                if (players != null && !players.isEmpty()) {
                    System.out.println(ConsoleHelper.info("Cascading preset to " + players.size() + " players..."));
                    int playersAffected = 0;

                    for (Object player : players) {
                        for (Map.Entry<String, Object> entry : preset.getPlayer_attributes().entrySet()) {
                            String readableKey = entry.getKey();
                            Object value = entry.getValue();

                            String obfuscatedField = resolveField(readableKey);
                            if (obfuscatedField != null) {
                                try {
                                    if (value instanceof Number) {
                                        Field f = player.getClass().getDeclaredField(obfuscatedField);
                                        f.setAccessible(true);
                                        if (f.getType() == int.class || f.getType() == Integer.class) {
                                            value = ((Number) value).intValue();
                                        } else if (f.getType() == long.class || f.getType() == Long.class) {
                                            value = ((Number) value).longValue();
                                        }
                                    }
                                    ReflectionUtils.setFieldValue(player, obfuscatedField, value);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                        playersAffected++;
                    }
                    System.out.println(ConsoleHelper.success("Applied attributes to " + playersAffected + " players."));
                }
            } catch (Exception e) {
                System.out.println(ConsoleHelper.error("failed to cascade to players: " + e.getMessage()));
            }
        }
    }

    public void applyPresetToAllInCountry(Object root, Object referenceTeam, String presetName) {
        Preset preset = loadPreset(presetName);
        if (preset == null) {
            System.out.println(ConsoleHelper.error("Preset not found: " + presetName));
            return;
        }

        try {
            int targetCountryId = (int) ReflectionUtils.getFieldValue(referenceTeam, BrasfootConstants.TEAM_COUNTRY);
            List<?> allTeams = (List<?>) ReflectionUtils.getFieldValue(root, BrasfootConstants.TEAMS_LIST);

            if (allTeams == null)
                return;

            int count = 0;
            System.out.println(ConsoleHelper.info(
                    "Applying preset '" + presetName + "' to ALL teams in country ID " + targetCountryId + "..."));

            for (Object team : allTeams) {
                // Check country
                try {
                    int cId = (int) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_COUNTRY);
                    if (cId == targetCountryId) {
                        applyPreset(team, presetName);
                        count++;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
            System.out.println(ConsoleHelper.success("Finished! Applied preset to " + count + " teams."));

        } catch (Exception e) {
            System.out.println(ConsoleHelper.error("Error applying to country: " + e.getMessage()));
        }
    }

    public void applyPresetToOthersInCountry(Object root, Object referenceTeam, String presetName) {
        Preset preset = loadPreset(presetName);
        if (preset == null) {
            System.out.println(ConsoleHelper.error("Preset not found: " + presetName));
            return;
        }

        try {
            int targetCountryId = (int) ReflectionUtils.getFieldValue(referenceTeam, BrasfootConstants.TEAM_COUNTRY);
            List<?> allTeams = (List<?>) ReflectionUtils.getFieldValue(root, BrasfootConstants.TEAMS_LIST);

            if (allTeams == null)
                return;

            int count = 0;
            System.out.println(ConsoleHelper.info("Applying preset '" + presetName
                    + "' to all OTHER teams in country ID " + targetCountryId + "..."));

            for (Object team : allTeams) {
                // Skip if it is the reference team itself
                if (team == referenceTeam)
                    continue;

                // Check country
                try {
                    int cId = (int) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_COUNTRY);
                    if (cId == targetCountryId) {
                        applyPreset(team, presetName); // Re-use existing method (which handles logging per team, might
                                                       // be too verbose, but safe)
                        count++;
                    }
                } catch (Exception e) {
                    // Ignore teams without country or errors
                }
            }
            System.out.println(ConsoleHelper.success("Finished! Applied preset to " + count + " rivals."));

        } catch (Exception e) {
            System.out.println(ConsoleHelper.error("Error applying to others: " + e.getMessage()));
        }
    }

    private String resolveField(String constantName) {
        try {
            Field field = BrasfootConstants.class.getField(constantName);
            return (String) field.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    // Capture current object state into a preset
    public Preset createPresetFromCurrent(Object target, String name, String type) {
        Preset preset = new Preset(name, type);

        try {
            // Iterate over constants to find relevant fields
            for (Field constant : BrasfootConstants.class.getFields()) {
                String key = constant.getName(); // e.g. PLAYER_AGE

                // Filter by type prefix
                if (type.equals("PLAYER") && !key.startsWith("PLAYER_"))
                    continue;
                if (type.equals("TEAM") && !key.startsWith("TEAM_"))
                    continue;

                String obfuscatedName = (String) constant.get(null);

                try {
                    Object val = ReflectionUtils.getFieldValue(target, obfuscatedName);
                    if (val != null && (val instanceof Number || val instanceof String || val instanceof Boolean)) {
                        preset.addAttribute(key, val);
                    }
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preset;
    }
}
