package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.SaveFileService;
import br.com.saveeditor.brasfoot.util.Config;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.util.Optional;

@ShellComponent
public class GeneralCommands {

    private final SaveFileService saveService;
    private final EditorShellContext shellContext;

    public GeneralCommands(SaveFileService saveService, EditorShellContext shellContext) {
        this.saveService = saveService;
        this.shellContext = shellContext;
    }

    @ShellMethod(key = { "load-file", "use", "open" }, value = "Load a save file")
    public String loadFile(@ShellOption(defaultValue = ShellOption.NULL) String path) {
        if (path == null) {
            path = Config.getDefaultSavePath();
            if (path == null) {
                return "No default path found. Please provide a path.";
            }
        }

        // Simple quote removal
        path = path.replace("\"", "");
        path = resolvePath(path);

        try {
            Optional<NavegacaoState> state = saveService.carregarSave(path);
            if (state.isPresent()) {
                shellContext.load(state.get(), path);
                Config.setDefaultSavePath(path);
                return "Loaded successfully: " + path;
            } else {
                return "Failed to load file.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @ShellMethod(key = "save-file", value = "Save changes to file")
    public String saveFile(@ShellOption(defaultValue = ShellOption.NULL) String filename) {
        if (!shellContext.isLoaded())
            return "No save loaded.";

        if (filename == null) {
            filename = new File(shellContext.getCurrentFilePath()).getName();
        }
        if (!filename.endsWith(".s22"))
            filename += ".s22";

        try {
            saveService.salvarSave(shellContext.getState(), filename);
            return "Saved to " + filename;
        } catch (Exception e) {
            return "Error saving: " + e.getMessage();
        }
    }

    private String resolvePath(String path) {
        // Only modify if it looks like a Windows path (C:\...) and we are on Linux
        // (WSL)
        String os = System.getProperty("os.name").toLowerCase();
        if ((os.contains("nix") || os.contains("nux") || os.contains("mac")) && path.matches("^[a-zA-Z]:[\\\\/].*")) {
            // Convert C:\Path\To\File -> /mnt/c/Path/To/File
            char drive = Character.toLowerCase(path.charAt(0));
            String rest = path.substring(3).replace('\\', '/');
            String wslPath = "/mnt/" + drive + "/" + rest;
            System.out.println("ℹ️  Converting Windows path to WSL: " + wslPath);
            return wslPath;
        }
        return path;
    }
}
