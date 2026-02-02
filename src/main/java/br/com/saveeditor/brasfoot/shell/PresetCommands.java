package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.model.Preset;
import br.com.saveeditor.brasfoot.service.PresetService;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import java.util.List;

@ShellComponent
public class PresetCommands {

    private final PresetService presetService;
    private final EditorShellContext shellContext;

    public PresetCommands(PresetService presetService, EditorShellContext shellContext) {
        this.presetService = presetService;
        this.shellContext = shellContext;
    }

    @ShellMethod(key = "presets", value = "List all available presets")
    public String listPresets() {
        List<String> presets = presetService.listPresets();
        if (presets.isEmpty()) {
            return ConsoleHelper.warning("No presets found in 'presets/' folder.");
        }
        return ConsoleHelper.info("Available Presets:\n" + String.join("\n- ", presets));
    }

    @ShellMethod(key = "create-preset", value = "Create a preset from current object. Usage: create-preset <name>")
    public String createPreset(String name) {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();
        String type;

        // Determine type
        if (isType(current, BrasfootConstants.PLAYER_NAME)) {
            type = "PLAYER";
        } else if (isType(current, BrasfootConstants.TEAM_NAME)) {
            type = "TEAM";
        } else {
            return ConsoleHelper.error("Current object is neither a Player nor a Team.");
        }

        Preset preset = presetService.createPresetFromCurrent(current, name, type);
        presetService.savePreset(preset);
        return ConsoleHelper.success("Preset '" + name + "' created from current " + type + ".");
    }

    @ShellMethod(key = "apply-preset", value = "Apply a preset to current object. Usage: apply-preset <name>")
    public String applyPreset(String name) {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();
        presetService.applyPreset(current, name);
        return "Done.";
    }

    @ShellMethod(key = "apply-preset-others", value = "Apply a preset to ALL OTHER teams in the same country as the current team. usage: apply-preset-others <preset>")
    public String applyPresetOthers(String presetName) {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();

        // Ensure we have a reference team
        if (!isType(current, BrasfootConstants.TEAM_NAME)) {
            return ConsoleHelper.error("You must be inside a Team to reference its country.");
        }

        presetService.applyPresetToOthersInCountry(shellContext.getState().getObjetoRaiz(), current, presetName);
        return ConsoleHelper.success("Batch operation completed.");
    }

    @ShellMethod(key = "apply-preset-country", value = "Apply a preset to ALL teams in the same country as the current team (INCLUDING current). usage: apply-preset-country <preset>")
    public String applyPresetCountry(String presetName) {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();

        if (!isType(current, BrasfootConstants.TEAM_NAME)) {
            return ConsoleHelper.error("You must be inside a Team to reference its country.");
        }

        presetService.applyPresetToAllInCountry(shellContext.getState().getObjetoRaiz(), current, presetName);
        return ConsoleHelper.success("Country-wide operation completed.");
    }

    private boolean isType(Object obj, String checkConstant) {
        try {
            ReflectionUtils.getFieldValue(obj, checkConstant);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
