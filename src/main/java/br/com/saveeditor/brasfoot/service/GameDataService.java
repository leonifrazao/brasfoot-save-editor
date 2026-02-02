package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import br.com.saveeditor.brasfoot.util.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GameDataService {

    /**
     * Retrieves the list of all teams from the root save object.
     */
    public List<Object> getTeams(Object root) {
        try {
            return (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.TEAMS_LIST);
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Error retrieving teams: " + e.getMessage()));
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves the list of all managers from the root save object.
     */
    public List<Object> getManagers(Object root) {
        try {
            return (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.HUMAN_MANAGERS_LIST); // Assuming
                                                                                                              // AL is
                                                                                                              // managers
                                                                                                              // list
        } catch (Exception e) {
            System.err.println(ConsoleHelper.error("Error retrieving managers: " + e.getMessage()));
            return Collections.emptyList();
        }
    }

    /**
     * Finds the first human manager in the save.
     */
    public Object getHumanManager(Object root) {
        List<Object> managers = getManagers(root);
        for (Object mgr : managers) {
            try {
                Boolean isHuman = (Boolean) ReflectionUtils.getFieldValue(mgr, BrasfootConstants.MANAGER_IS_HUMAN);
                if (Boolean.TRUE.equals(isHuman)) {
                    return mgr;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Finds the team controlled by the human manager.
     */
    public Object getHumanTeam(Object root) {
        Object humanManager = getHumanManager(root);
        if (humanManager == null)
            return null;

        try {
            // Manager matches Team via ID.
            // Manager field 'nU' seems to be Team ID based on analysis.
            int teamId = (int) ReflectionUtils.getFieldValue(humanManager, "nU");
            return getTeamById(root, teamId);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Finds a team by its ID.
     */
    public Object getTeamById(Object root, int id) {
        List<Object> teams = getTeams(root);
        for (Object team : teams) {
            try {
                int tId = (int) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_ID);
                if (tId == id)
                    return team;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * Retrieves the players list for a given team.
     */
    public List<Object> getPlayers(Object team) {
        try {
            return (List<Object>) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_PLAYERS);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Finds a team index by name (fuzzy search).
     */
    public int findTeamIndex(Object root, String term) {
        List<Object> teams = getTeams(root);
        if (teams.isEmpty())
            return -1;
        String termNormalized = StringUtils.normalize(term);

        // Pass 1: Exact
        for (int i = 0; i < teams.size(); i++) {
            if (matchTeamName(teams.get(i), termNormalized, true))
                return i;
        }
        // Pass 2: Starts With
        for (int i = 0; i < teams.size(); i++) {
            if (matchTeamName(teams.get(i), termNormalized, false))
                return i;
        }
        // Pass 3: Contains
        for (int i = 0; i < teams.size(); i++) {
            if (nameContains(teams.get(i), termNormalized))
                return i;
        }
        return -1;
    }

    private boolean matchTeamName(Object team, String termNorm, boolean exact) {
        try {
            String name = (String) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_NAME);
            String nameNorm = StringUtils.normalize(name);
            return exact ? nameNorm.equals(termNorm) : nameNorm.startsWith(termNorm);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean nameContains(Object team, String termNorm) {
        try {
            String name = (String) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_NAME);
            return StringUtils.normalize(name).contains(termNorm);
        } catch (Exception e) {
            return false;
        }
    }
}
