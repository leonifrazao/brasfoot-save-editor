package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.service.DebugService;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.lang.reflect.Field;
import java.util.List;

@ShellComponent
public class DebugCommands {

    private final EditorShellContext shellContext;
    private final DebugService debugService;

    public DebugCommands(EditorShellContext shellContext, DebugService debugService) {
        this.shellContext = shellContext;
        this.debugService = debugService;
    }

    @ShellMethod(key = "search-value", value = "Search for a value recursively. Usage: search-value <val> [depth]")
    public String searchValue(String value, @ShellOption(defaultValue = "2") int depth) {
        if (!shellContext.isLoaded()) {
            return ConsoleHelper.error("No save loaded.");
        }

        Object current = shellContext.getState().getObjetoAtual();
        System.out.println(ConsoleHelper.info("Deep searching for '" + value + "' starting at "
                + current.getClass().getSimpleName() + " (Depth: " + depth + ")..."));

        List<String> results = debugService.searchRecursive(current, value, depth);

        if (results.isEmpty()) {
            return ConsoleHelper.warning("No matches found.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(ConsoleHelper.success("Found " + results.size() + " matches:\n"));
        for (String res : results) {
            sb.append(res).append("\n");
        }
        return sb.toString();
    }
}
