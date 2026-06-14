package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateTeamUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TeamManagementService implements GetTeamUseCase, UpdateTeamUseCase {

    private static final Logger log = LoggerFactory.getLogger(TeamManagementService.class);
    private static final int STADIUM_SECTOR_COUNT = 4;

    private final GameDataPort gameDataPort;
    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;

    public TeamManagementService(SessionStatePort sessionStatePort, GameDataPort gameDataPort,
                                 SessionResolver sessionResolver) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataPort = gameDataPort;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public List<Team> getAllTeams(UUID sessionId) {
        log.debug("Fetching all teams for session {}", sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        List<Object> teamObjects = gameDataPort.getTeams(root);

        return teamObjects.stream()
                .map(this::mapToTeamDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Team getTeam(UUID sessionId, int teamId) {
        log.debug("Fetching team {} in session {}", teamId, sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        return mapToTeamDomain(teamObj);
    }

    public Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation) {
        return updateTeam(sessionId, teamId, money, reputation, null, null);
    }

    public Team updateTeam(UUID sessionId, int teamId, String name, String alias, Long money, TeamReputation reputation,
                           String stadiumName, List<Integer> stadiumSectors, Integer country, Integer division,
                           Integer level, Integer tacticStyle, Integer tacticMarking, Integer tacticFocus) {
        Team updated = updateTeam(sessionId, teamId, money, reputation, stadiumName, stadiumSectors);
        Session session = sessionResolver.loadRequired(sessionId);
        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        try {
            setIfPresent(teamObj, BrasfootConstants.TEAM_NAME, name);
            setIfPresent(teamObj, BrasfootConstants.TEAM_ALIAS, alias);
            setIfPresent(teamObj, BrasfootConstants.TEAM_COUNTRY, country);
            setIfPresent(teamObj, BrasfootConstants.TEAM_DIVISION, division);
            setIfPresent(teamObj, BrasfootConstants.TEAM_LEVEL, level);
            updateTactics(teamObj, tacticStyle, tacticMarking, tacticFocus);
            sessionStatePort.save(session);
            return mapToTeamDomain(teamObj);
        } catch (Exception e) {
            log.error("Failed to update extended team properties", e);
            throw new RuntimeException("Failed to update extended team properties", e);
        }
    }

    @Override
    public Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation, String stadiumName,
                           List<Integer> stadiumSectors) {
        log.info("Updating team {} for session {}", teamId, sessionId);
        log.debug("Update details - money: {}, reputation: {}, stadiumName: {}, stadiumSectors: {}",
                money, reputation, stadiumName, stadiumSectors);

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        Team currentTeam = mapToTeamDomain(teamObj);

        if (money != null) {
            Team.builder()
                    .id(currentTeam.getId())
                    .name(currentTeam.getName())
                    .money(money)
                    .reputation(currentTeam.getReputation())
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

        updateStadium(teamObj, stadiumName, stadiumSectors);

        sessionStatePort.save(session);

        return mapToTeamDomain(teamObj);
    }

    @Override
    public BatchResponse<Team> batchUpdateTeams(UUID sessionId, List<TeamBatchUpdateCommand> commands) {
        log.info("Batch updating {} teams for session {}", commands.size(), sessionId);

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        List<BatchResult<Team>> results = new java.util.ArrayList<>();

        for (int i = 0; i < commands.size(); i++) {
            var command = commands.get(i);
            try {
                Object teamObj = gameDataPort.getTeamById(root, command.teamId());
                if (teamObj == null) {
                    log.warn("Team not found with ID: {}", command.teamId());
                    results.add(BatchResult.failure(i, "Team not found with ID: " + command.teamId()));
                    continue;
                }

                Team currentTeam = mapToTeamDomain(teamObj);

                if (command.money() != null) {
                    Team.builder()
                            .id(currentTeam.getId())
                            .name(currentTeam.getName())
                            .money(command.money())
                            .reputation(currentTeam.getReputation())
                            .build();
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MONEY, command.money());
                }
                if (command.reputation() != null) {
                    ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION, command.reputation().getValue());
                }
                updateStadium(teamObj, command.stadiumName(), command.stadiumSectors());
                setIfPresent(teamObj, BrasfootConstants.TEAM_LEVEL, command.level());

                results.add(BatchResult.success(i, mapToTeamDomain(teamObj)));
            } catch (IllegalArgumentException e) {
                log.warn("Validation error during batch team update: {}", e.getMessage());
                results.add(BatchResult.failure(i, e.getMessage()));
            } catch (Exception e) {
                log.error("Failed to update team properties for team {}", command.teamId(), e);
                results.add(BatchResult.failure(i, "Failed to update team properties: " + e.getMessage()));
            }
        }

        sessionStatePort.save(session);
        return new BatchResponse<>(results);
    }

    private Team mapToTeamDomain(Object teamObj) {
        try {
            int id = (int) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_ID);
            String name = (String) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_NAME);
            String alias = stringField(teamObj, BrasfootConstants.TEAM_ALIAS);
            long money = (long) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_MONEY);
            int reputationInt = (int) ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_REPUTATION);
            Integer country = integerField(teamObj, BrasfootConstants.TEAM_COUNTRY);
            Integer division = integerField(teamObj, BrasfootConstants.TEAM_DIVISION);
            Integer level = integerField(teamObj, BrasfootConstants.TEAM_LEVEL);
            int[] tactics = intArrayField(teamObj, BrasfootConstants.TEAM_TACTICS);
            Object stadiumObj = ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_STADIUM);
            String stadiumName = null;
            Integer stadiumCapacity = null;
            List<Integer> stadiumSectors = null;

            if (stadiumObj != null) {
                stadiumName = (String) ReflectionUtils.getFieldValue(stadiumObj, BrasfootConstants.STADIUM_NAME);
                int[] sectors = (int[]) ReflectionUtils.getFieldValue(stadiumObj, BrasfootConstants.STADIUM_SECTORS);
                if (sectors != null) {
                    stadiumSectors = Arrays.stream(sectors).boxed().toList();
                    stadiumCapacity = Arrays.stream(sectors).sum();
                }
            }

            // Sanitize negative money values to 0 (game data corruption safety)
            if (money < 0) {
                log.warn("Team {} has negative money value ({}), sanitizing to 0", id, money);
                money = 0;
            }

            Team team = new Team(id, name, money, TeamReputation.fromValue(reputationInt), stadiumName,
                    stadiumCapacity, stadiumSectors);
            team.setAlias(alias);
            team.setCountry(country);
            team.setDivision(division);
            team.setLevel(level);
            if (tactics != null && tactics.length > 3) {
                team.setTacticStyle(tactics[1]);
                team.setTacticMarking(tactics[2]);
                team.setTacticFocus(tactics[3]);
            }
            return team;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map team object to domain", e);
        }
    }

    private void setIfPresent(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        if (value != null) {
            ReflectionUtils.setFieldValue(target, fieldName, value);
        }
    }

    private void updateTactics(Object teamObj, Integer tacticStyle, Integer tacticMarking, Integer tacticFocus) throws ReflectiveOperationException {
        if (tacticStyle == null && tacticMarking == null && tacticFocus == null) {
            return;
        }

        int[] tactics = intArrayField(teamObj, BrasfootConstants.TEAM_TACTICS);
        if (tactics == null || tactics.length < 4) {
            tactics = new int[4];
        }
        if (tacticStyle != null) {
            tactics[1] = tacticStyle;
        }
        if (tacticMarking != null) {
            tactics[2] = tacticMarking;
        }
        if (tacticFocus != null) {
            tactics[3] = tacticFocus;
        }
        ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_TACTICS, tactics);
    }

    private String stringField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return value == null ? null : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private Integer integerField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return value == null ? null : ((Number) value).intValue();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private int[] intArrayField(Object obj, String fieldName) {
        try {
            return (int[]) ReflectionUtils.getFieldValue(obj, fieldName);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private void updateStadium(Object teamObj, String stadiumName, List<Integer> stadiumSectors) {
        if (stadiumName == null && stadiumSectors == null) {
            return;
        }

        try {
            Object stadiumObj = ReflectionUtils.getFieldValue(teamObj, BrasfootConstants.TEAM_STADIUM);
            if (stadiumObj == null) {
                throw new IllegalArgumentException("Team has no stadium object");
            }

            if (stadiumName != null) {
                String normalizedName = stadiumName.trim();
                if (normalizedName.isEmpty()) {
                    throw new IllegalArgumentException("Stadium name cannot be blank");
                }
                ReflectionUtils.setFieldValue(stadiumObj, BrasfootConstants.STADIUM_NAME, normalizedName);
            }

            if (stadiumSectors != null) {
                int[] sectors = toStadiumSectors(stadiumSectors);
                ReflectionUtils.setFieldValue(stadiumObj, BrasfootConstants.STADIUM_SECTORS, sectors);
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update stadium", e);
        }
    }

    private int[] toStadiumSectors(List<Integer> stadiumSectors) {
        if (stadiumSectors.size() != STADIUM_SECTOR_COUNT) {
            throw new IllegalArgumentException("Stadium sectors must contain exactly 4 values");
        }

        int[] sectors = new int[STADIUM_SECTOR_COUNT];
        for (int i = 0; i < STADIUM_SECTOR_COUNT; i++) {
            Integer sector = stadiumSectors.get(i);
            if (sector == null || sector < 0) {
                throw new IllegalArgumentException("Stadium sectors cannot contain negative values");
            }
            sectors[i] = sector;
        }

        int capacity = Arrays.stream(sectors).sum();
        if (capacity <= 0) {
            throw new IllegalArgumentException("Stadium capacity must be greater than zero");
        }

        return sectors;
    }
}
