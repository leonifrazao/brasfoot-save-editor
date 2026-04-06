package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.TeamReputation;
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
    private final GameDataPort gameDataPort;

    public TeamManagementService(SessionStatePort sessionStatePort, GameDataPort gameDataPort) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataPort = gameDataPort;
    }

    @Override
    public List<Team> getAllTeams(UUID sessionId) {
        log.debug("Fetching all teams for session {}", sessionId);
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        List<Object> teamObjects = gameDataPort.getTeams(root);

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
        Object teamObj = gameDataPort.getTeamById(root, teamId);
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
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        Team currentTeam = mapToTeamDomain(teamObj);

        if (money != null) {
            Team.builder()
                    .id(currentTeam.id())
                    .name(currentTeam.name())
                    .money(money)
                    .reputation(currentTeam.reputation())
                    .build();
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
    public List<Team> batchUpdateTeams(UUID sessionId, List<TeamBatchUpdateCommand> commands) {
        log.info("Batch updating {} teams for session {}", commands.size(), sessionId);
        
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        List<Team> updatedTeams = new java.util.ArrayList<>();

        for (var command : commands) {
            Object teamObj = gameDataPort.getTeamById(root, command.teamId());
            if (teamObj == null) {
                log.warn("Team not found with ID: {}", command.teamId());
                continue;
            }

            Team currentTeam = mapToTeamDomain(teamObj);

            if (command.money() != null) {
                Team.builder()
                        .id(currentTeam.id())
                        .name(currentTeam.name())
                        .money(command.money())
                        .reputation(currentTeam.reputation())
                        .build();
                try {
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MONEY, command.money());
                } catch (Exception e) {
                    log.error("Failed to update team money for team {}", command.teamId(), e);
                    throw new RuntimeException("Failed to update team money", e);
                }
            }

            if (command.reputation() != null) {
                try {
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION, command.reputation().getValue());
                } catch (Exception e) {
                    log.error("Failed to update team reputation for team {}", command.teamId(), e);
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
