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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamManagementService implements GetTeamUseCase, UpdateTeamUseCase {

    private static final Logger log = LoggerFactory.getLogger(TeamManagementService.class);

    private final SessionStatePort sessionStatePort;
    private final GameDataService gameDataService;

    public TeamManagementService(SessionStatePort sessionStatePort, GameDataService gameDataService) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataService = gameDataService;
    }

    @Override
    public List<Team> getAllTeams(UUID sessionId) {
        log.debug("Fetching all teams for session {}", sessionId);
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
        log.debug("Fetching team {} in session {}", teamId, sessionId);
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
        log.info("Updating team {} for session {}", teamId, sessionId);
        log.debug("Update details - money: {}, reputation: {}", money, reputation);

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
                log.error("Failed to update team money", e);
                throw new RuntimeException("Failed to update team money", e);
            }
        }

        if (reputation != null) {
            try {
                ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION, reputation.getValue());
            } catch (Exception e) {
                log.error("Failed to update team reputation", e);
                throw new RuntimeException("Failed to update team reputation", e);
            }
        }

        sessionStatePort.save(session);

        return mapToTeamDomain(teamObj);
    }

    @Override
    public List<Team> batchUpdateTeams(UUID sessionId, List<br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamBatchUpdateRequest> requests) {
        log.info("Batch updating {} teams for session {}", requests.size(), sessionId);
        
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        List<Team> updatedTeams = new java.util.ArrayList<>();

        for (var request : requests) {
            Object teamObj = gameDataService.getTeamById(root, request.teamId());
            if (teamObj == null) {
                log.warn("Team not found with ID: {}", request.teamId());
                continue;
            }

            if (request.money() != null) {
                if (request.money() < 0) {
                    throw new IllegalArgumentException("Money cannot be negative for team " + request.teamId());
                }
                try {
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MONEY, request.money());
                } catch (Exception e) {
                    log.error("Failed to update team money for team {}", request.teamId(), e);
                    throw new RuntimeException("Failed to update team money", e);
                }
            }

            if (request.reputation() != null) {
                try {
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION, request.reputation().getValue());
                } catch (Exception e) {
                    log.error("Failed to update team reputation for team {}", request.teamId(), e);
                    throw new RuntimeException("Failed to update team reputation", e);
                }
            }

            updatedTeams.add(mapToTeamDomain(teamObj));
        }

        sessionStatePort.save(session);
        return updatedTeams;
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
