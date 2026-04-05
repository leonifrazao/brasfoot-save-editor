package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.TeamReputation;
import br.com.saveeditor.brasfoot.service.GameDataService;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamManagementService implements GetTeamUseCase, UpdateTeamUseCase {

    private final SessionStatePort sessionStatePort;
    private final GameDataService gameDataService;

    public TeamManagementService(SessionStatePort sessionStatePort, GameDataService gameDataService) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataService = gameDataService;
    }

    @Override
    public List<Team> getAllTeams(UUID sessionId) {
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        List<Object> teamObjects = gameDataService.getTeams(root);

        return teamObjects.stream()
                .map(this::mapToTeamDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Team getTeam(UUID sessionId, int teamId) {
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        Object teamObj = gameDataService.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        return mapToTeamDomain(teamObj);
    }

    @Override
    public Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation) {
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        Object teamObj = gameDataService.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        if (money != null) {
            if (money < 0) {
                throw new IllegalArgumentException("Money cannot be negative");
            }
            try {
                ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MONEY, money);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update team money", e);
            }
        }

        if (reputation != null) {
            try {
                ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION, reputation.getValue());
            } catch (Exception e) {
                throw new RuntimeException("Failed to update team reputation", e);
            }
        }

        sessionStatePort.save(session);

        return mapToTeamDomain(teamObj);
    }

    private Team mapToTeamDomain(Object teamObj) {
        try {
            int id = (int) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_ID);
            String name = (String) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_NAME);
            long money = (long) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_MONEY);
            int reputationInt = (int) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION);

            return new Team(id, name, money, TeamReputation.fromValue(reputationInt));
        } catch (Exception e) {
            throw new RuntimeException("Failed to map team object to domain", e);
        }
    }
}
