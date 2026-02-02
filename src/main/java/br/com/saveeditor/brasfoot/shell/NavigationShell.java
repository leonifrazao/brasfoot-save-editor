package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.EditorService;
import br.com.saveeditor.brasfoot.service.GameDataService;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.util.Collection;

@ShellComponent
public class NavigationShell {

    private final EditorService editorService;
    private final EditorShellContext shellContext;
    private final GameDataService gameDataService;

    public NavigationShell(EditorService editorService, EditorShellContext shellContext,
            GameDataService gameDataService) {
        this.editorService = editorService;
        this.shellContext = shellContext;
        this.gameDataService = gameDataService;
    }

    @ShellMethod(key = "ls", value = "List fields and values of the current object")
    public String ls() {
        if (!shellContext.isLoaded()) {
            return ConsoleHelper.error("No save file loaded. Use 'load-file <path>' first.");
        }

        NavegacaoState state = shellContext.getState();
        Object current = state.getObjetoAtual();
        StringBuilder sb = new StringBuilder();

        sb.append("Path: ").append(ConsoleHelper.info(state.getCaminhoAtual())).append("\n");
        sb.append("Class: ").append(current.getClass().getName()).append("\n\n");
        sb.append(ConsoleHelper.renderTable(current));

        return sb.toString();
    }

    @ShellMethod(key = { "cd", "enter" }, value = "Navigate into a field or list index")
    public String cd(@ShellOption(help = "Field name or index") String target) {
        if (!shellContext.isLoaded())
            return "No save file loaded.";

        try {
            NavegacaoState state = shellContext.getState();
            Object current = state.getObjetoAtual();

            if (target.equals("..")) {
                state.voltar();
                return "Moved up to: " + state.getCaminhoAtual();
            }

            // Check if index
            if (current instanceof Collection || current.getClass().isArray()) {
                editorService.entrarEmItemDeLista(state, target);
            } else {
                editorService.entrarEmCampo(state, target);
            }
            return "Entered: " + state.getCaminhoAtual();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @ShellMethod(key = "pwd", value = "Print current path")
    public String pwd() {
        if (!shellContext.isLoaded())
            return "No save file loaded.";
        return shellContext.getState().getCaminhoAtual();
    }

    @ShellMethod(key = { "back", ".." }, value = "Go back one level")
    public String back() {
        if (!shellContext.isLoaded())
            return "No save file loaded.";
        shellContext.getState().voltar();
        return "Moved up to: " + shellContext.getState().getCaminhoAtual();
    }

    @ShellMethod(key = { "update", "set" }, value = "Update a field value. Usage: set <field> <value>")
    public String update(String field, String value) {
        if (!shellContext.isLoaded())
            return "No save file loaded.";

        try {
            String arg = field + "=" + value;
            editorService.modificarValor(shellContext.getState().getObjetoAtual(), arg);
            return "Updated.";
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    @ShellMethod(key = { "goto-team", "find-team" }, value = "Navigate directly to a team by name")
    public String gotoTeam(String name) {
        if (!shellContext.isLoaded())
            return "No save file loaded.";

        try {
            NavegacaoState state = shellContext.getState();
            Object root = state.getObjetoRaiz();

            // Search team index
            int index = gameDataService.findTeamIndex(root, name);
            if (index == -1) {
                return "Team '" + name + "' not found.";
            }

            // Reset navigation to root
            state.limparNavegacaoEReiniciar();

            // Enter 'aj' (teams list)
            editorService.entrarEmCampo(state, "aj");

            // Enter specific index
            editorService.entrarEmItemDeLista(state, String.valueOf(index));

            // Try to get team name for success message
            Object current = state.getObjetoAtual();
            String teamName = (String) ReflectionUtils.getFieldValue(current, "dm");

            return "Navigated to team: " + teamName + " (at index " + index + ")";

        } catch (Exception e) {
            return "Error navigating: " + e.getMessage();
        }
    }
}
