package br.com.saveeditor.brasfoot.shell;

import br.com.saveeditor.brasfoot.service.CheatService;
import br.com.saveeditor.brasfoot.service.GameDataService;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class CheatCommands {

    private final CheatService cheatService;
    private final EditorShellContext shellContext;
    private final GameDataService gameDataService;

    public CheatCommands(CheatService cheatService, EditorShellContext shellContext,
            GameDataService gameDataService) {
        this.cheatService = cheatService;
        this.shellContext = shellContext;
        this.gameDataService = gameDataService;
    }

    @ShellMethod(key = "inject-money", value = "Inject 2 Billion into current team or human team")
    public String injectMoney() {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();
        Object targetTeam = current;

        // 1. Try to use current object if it's a team
        if (!isTeam(current)) {
            // 2. Not a team? Try finding Human Team
            targetTeam = gameDataService.getHumanTeam(shellContext.getState().getObjetoRaiz());
            if (targetTeam != null) {
                System.out.println(ConsoleHelper.info("Auto-detected Human Team. Applying cheat..."));
            }
        }

        if (targetTeam == null || !isTeam(targetTeam)) {
            return ConsoleHelper
                    .error("Could not find a valid team. Navigate to a team or ensure you have a human manager.");
        }

        try {
            cheatService.injectMoney(targetTeam, 2000000000L);
            return ConsoleHelper.success("Money injected!");
        } catch (Exception e) {
            return ConsoleHelper.error("Failed: " + e.getMessage());
        }
    }

    private boolean isTeam(Object obj) {
        // Simple check: does it have a 'dm' (name) and 'nb' (money) field?
        try {
            ReflectionUtils.getFieldValue(obj, BrasfootConstants.TEAM_NAME);
            ReflectionUtils.getFieldValue(obj, BrasfootConstants.TEAM_MONEY);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @ShellMethod(key = "max-reputation", value = "Maximize reputation of current team")
    public String maxReputation() {
        if (!shellContext.isLoaded())
            return "No save loaded.";
        try {
            cheatService.maxReputation(shellContext.getState().getObjetoAtual());
            return "Reputation maximized!";
        } catch (Exception e) {
            return "Failed. Navigate to a Team object first.";
        }
    }

    @ShellMethod(key = "heal-team", value = "Heal all players of current team")
    public String healTeam() {
        if (!shellContext.isLoaded())
            return "No save loaded.";
        try {
            cheatService.healTeam(shellContext.getState().getObjetoAtual());
            return "Team healed!";
        } catch (Exception e) {
            return "Failed. Navigate to a Team object first.";
        }
    }

    @ShellMethod(key = "clone-player", value = "Clone a player from any team to your team. Usage: clone-player <PlayerName>")
    public String clonePlayer(String playerName) {
        if (!shellContext.isLoaded())
            return ConsoleHelper.error("No save loaded.");

        Object current = shellContext.getState().getObjetoAtual();
        Object targetTeam = current;

        // Auto-detect human team if not inside one
        if (!isTeam(current)) {
            targetTeam = gameDataService.getHumanTeam(shellContext.getState().getObjetoRaiz());
        }

        if (targetTeam == null || !isTeam(targetTeam)) {
            return ConsoleHelper.error("Could not find your team. Load a save with a human manager.");
        }

        try {
            cheatService.clonePlayerToMyTeam(shellContext.getState().getObjetoRaiz(), targetTeam, playerName);
            return ConsoleHelper.success("Done.");
        } catch (Exception e) {
            return ConsoleHelper.error("Error: " + e.getMessage());
        }
    }
}
