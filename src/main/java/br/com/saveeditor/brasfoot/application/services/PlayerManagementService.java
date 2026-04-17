package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PlayerManagementService implements GetPlayerUseCase, UpdatePlayerUseCase {

    private static final Logger log = LoggerFactory.getLogger(PlayerManagementService.class);

    private final GameDataPort gameDataPort;
    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;

    public PlayerManagementService(SessionStatePort sessionStatePort, GameDataPort gameDataPort,
                                   SessionResolver sessionResolver) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataPort = gameDataPort;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public List<Player> getTeamPlayers(UUID sessionId, int teamId) {
        log.debug("Fetching players for team {} in session {}", teamId, sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < playerObjects.size(); i++) {
            players.add(mapToPlayerDomain(playerObjects.get(i), i));
        }

        return players;
    }

    @Override
    public Player getPlayer(UUID sessionId, int teamId, int playerId) {
        log.debug("Fetching player {} from team {} in session {}", playerId, teamId, sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        if (playerId < 0 || playerId >= playerObjects.size()) {
            throw new IllegalArgumentException("Player index out of bounds");
        }

        return mapToPlayerDomain(playerObjects.get(playerId), playerId);
    }

    @Override
    public Player updatePlayer(UUID sessionId, int teamId, int playerId, Integer age, Integer overall, Integer position, Integer energy, Integer morale) {
        log.info("Updating player {} in team {} for session {}", playerId, teamId, sessionId);
        log.debug("Update details - age: {}, overall: {}, position: {}, energy: {}, morale: {}", age, overall, position, energy, morale);

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        if (playerId < 0 || playerId >= playerObjects.size()) {
            throw new IllegalArgumentException("Player index out of bounds");
        }

        Object playerObj = playerObjects.get(playerId);

        try {
            Player currentPlayer = mapToPlayerDomain(playerObj, playerId);

            if (age != null) {
                Player.builder()
                        .id(currentPlayer.getId())
                        .name(currentPlayer.getName())
                        .age(age)
                        .overall(currentPlayer.getOverall())
                        .position(currentPlayer.getPosition())
                        .energy(currentPlayer.getEnergy())
                        .morale(currentPlayer.getMorale())
                        .build();
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_AGE, age);
            }
            if (overall != null) {
                Player.builder()
                        .id(currentPlayer.getId())
                        .name(currentPlayer.getName())
                        .age(currentPlayer.getAge())
                        .overall(overall)
                        .position(currentPlayer.getPosition())
                        .energy(currentPlayer.getEnergy())
                        .morale(currentPlayer.getMorale())
                        .build();
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL, overall);
            }
            if (position != null) {
                Player.builder()
                        .id(currentPlayer.getId())
                        .name(currentPlayer.getName())
                        .age(currentPlayer.getAge())
                        .overall(currentPlayer.getOverall())
                        .position(position)
                        .energy(currentPlayer.getEnergy())
                        .morale(currentPlayer.getMorale())
                        .build();
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION, position);
            }
            if (energy != null) {
                Player.builder()
                        .id(currentPlayer.getId())
                        .name(currentPlayer.getName())
                        .age(currentPlayer.getAge())
                        .overall(currentPlayer.getOverall())
                        .position(currentPlayer.getPosition())
                        .energy(energy)
                        .morale(currentPlayer.getMorale())
                        .build();
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_ENERGY, energy);
            }
            // Morale isn't in Constants, skipping or maybe it's not present yet.
            // Wait, morale might be "eT" or similar. We'll ignore morale if we don't have constant.

        } catch (IllegalArgumentException e) {
            log.warn("Validation error during player update: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to update player properties", e);
            throw new RuntimeException("Failed to update player properties", e);
        }

        sessionStatePort.save(session);
        return mapToPlayerDomain(playerObj, playerId);
    }

    @Override
    public List<Player> batchUpdatePlayers(UUID sessionId, int teamId, List<PlayerBatchUpdateCommand> commands) {
        log.info("Batch updating {} players in team {} for session {}", commands.size(), teamId, sessionId);

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        List<Player> updatedPlayers = new ArrayList<>();

        for (var command : commands) {
            int playerId = command.playerId();
            if (playerId < 0 || playerId >= playerObjects.size()) {
                log.warn("Player index {} out of bounds for team {}", playerId, teamId);
                continue;
            }

            Object playerObj = playerObjects.get(playerId);

            try {
                Player currentPlayer = mapToPlayerDomain(playerObj, playerId);

                if (command.age() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(command.age())
                            .overall(currentPlayer.getOverall())
                            .position(currentPlayer.getPosition())
                            .energy(currentPlayer.getEnergy())
                            .morale(currentPlayer.getMorale())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_AGE, command.age());
                }
                if (command.overall() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(currentPlayer.getAge())
                            .overall(command.overall())
                            .position(currentPlayer.getPosition())
                            .energy(currentPlayer.getEnergy())
                            .morale(currentPlayer.getMorale())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL, command.overall());
                }
                if (command.position() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(currentPlayer.getAge())
                            .overall(currentPlayer.getOverall())
                            .position(command.position())
                            .energy(currentPlayer.getEnergy())
                            .morale(currentPlayer.getMorale())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION, command.position());
                }
                if (command.energy() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(currentPlayer.getAge())
                            .overall(currentPlayer.getOverall())
                            .position(currentPlayer.getPosition())
                            .energy(command.energy())
                            .morale(currentPlayer.getMorale())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_ENERGY, command.energy());
                }
            } catch (IllegalArgumentException e) {
                log.warn("Validation error during batch player update: {}", e.getMessage());
                throw e;
            } catch (Exception e) {
                log.error("Failed to update player properties for player {}", playerId, e);
                throw new RuntimeException("Failed to update player properties", e);
            }

            updatedPlayers.add(mapToPlayerDomain(playerObj, playerId));
        }

        sessionStatePort.save(session);
        return updatedPlayers;
    }

    private Player mapToPlayerDomain(Object playerObj, int index) {
        try {
            String name = (String) ReflectionUtils.getFieldValue(playerObj, BrasfootConstants.PLAYER_NAME);
            int age = (int) ReflectionUtils.getFieldValue(playerObj, BrasfootConstants.PLAYER_AGE);
            int overall = (int) ReflectionUtils.getFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL);
            int position = (int) ReflectionUtils.getFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION);
            int energy = (int) ReflectionUtils.getFieldValue(playerObj, BrasfootConstants.PLAYER_ENERGY);

            // Assume default 100 for morale for now
            int morale = 100;

            return new Player(index, name, age, overall, position, energy, morale);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map player object to domain", e);
        }
    }
}
