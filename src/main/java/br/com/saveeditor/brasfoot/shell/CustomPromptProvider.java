package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.service.GameDataService;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class CustomPromptProvider implements PromptProvider {

    private final EditorShellContext shellContext;
    private final GameDataService gameDataService;

    public CustomPromptProvider(EditorShellContext shellContext, GameDataService gameDataService) {
        this.shellContext = shellContext;
        this.gameDataService = gameDataService;
    }

    @Override
    public AttributedString getPrompt() {
        if (!shellContext.isLoaded()) {
            return new AttributedString("brasfoot-editor> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
        }

        String path = shellContext.getState().getCaminhoAtual();
        String contextInfo = "";

        // Try to add context (e.g. Current Team Name)
        // If we are deep in structure, maybe show root team?
        // Let's try to show the Human Team name if loaded
        try {
            Object humanTeam = gameDataService.getHumanTeam(shellContext.getState().getObjetoRaiz());
            if (humanTeam != null) {
                String teamName = (String) ReflectionUtils.getFieldValue(humanTeam, BrasfootConstants.TEAM_NAME);
                contextInfo = "[" + teamName + "] ";
            }
        } catch (Exception ignored) {
        }

        return new AttributedString(contextInfo + "brasfoot:" + path + "> ",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
    }

    private String resolveName(Object obj) {
        try {
            // Try explicit TEAM_NAME or PLAYER_NAME constants first
            try {
                return (String) ReflectionUtils.getFieldValue(obj, BrasfootConstants.TEAM_NAME);
            } catch (Exception ignored) {
            }

            try {
                return (String) ReflectionUtils.getFieldValue(obj, BrasfootConstants.PLAYER_NAME);
            } catch (Exception ignored) {
            }

            // Fallback: check class name
            String className = obj.getClass().getName();
            if (className.contains("ArrayList"))
                return "List";

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
