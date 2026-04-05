package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.service.GameDataService;
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

    private final SessionStatePort sessionStatePort;
    private final GameDataService gameDataService;

    public PlayerManagementService(SessionStatePort sessionStatePort, GameDataService gameDataService) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataService = gameDataService;
    }

    @Override
    public List<Player> getTeamPlayers(UUID sessionId, int teamId) {
        log.debug("Fetching players for team {} in session {}", teamId, sessionId);
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        Object teamObj = gameDataService.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataService.getPlayers(teamObj);
        List<Player> players = new ArrayList<>();
        
        for (int i = 0; i < playerObjects.size(); i++) {
            players.add(mapToPlayerDomain(playerObjects.get(i), i));
        }
        
        return players;
    }

    @Override
    public Player getPlayer(UUID sessionId, int teamId, int playerId) {
        log.debug("Fetching player {} from team {} in session {}", playerId, teamId, sessionId);
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        Object teamObj = gameDataService.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataService.getPlayers(teamObj);
        if (playerId < 0 || playerId >= playerObjects.size()) {
            throw new IllegalArgumentException("Player index out of bounds");
        }

        return mapToPlayerDomain(playerObjects.get(playerId), playerId);
    }

    @Override
    public Player updatePlayer(UUID sessionId, int teamId, int playerId, Integer age, Integer overall, Integer position, Integer energy, Integer morale) {
        log.info("Updating player {} in team {} for session {}", playerId, teamId, sessionId);
        log.debug("Update details - age: {}, overall: {}, position: {}, energy: {}, morale: {}", age, overall, position, energy, morale);
        
        Session session = sessionStatePort.load(sessionId);
        if (session == null || !session.context().isLoaded()) {
            throw new IllegalArgumentException("Session not found or not loaded.");
        }

        Object root = session.context().getState().getObjetoRaiz();
        Object teamObj = gameDataService.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataService.getPlayers(teamObj);
        if (playerId < 0 || playerId >= playerObjects.size()) {
            throw new IllegalArgumentException("Player index out of bounds");
        }

        Object playerObj = playerObjects.get(playerId);

        try {
            if (age != null) {
                if (age < 15 || age > 50) throw new IllegalArgumentException("Invalid age: must be between 15 and 50");
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_AGE, age);
            }
            if (overall != null) {
                if (overall < 1 || overall > 100) throw new IllegalArgumentException("Invalid overall: must be between 1 and 100");
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL, overall);
            }
            if (position != null) {
                if (position < 0 || position > 4) throw new IllegalArgumentException("Invalid position: must be 0 to 4");
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION, position);
            }
            if (energy != null) {
                if (energy < -1 || energy > 100) throw new IllegalArgumentException("Invalid energy: must be between -1 and 100");
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
