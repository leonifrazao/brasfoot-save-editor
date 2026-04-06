package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerManagementService implements GetManagerUseCase, UpdateManagerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManagerManagementService.class);

    private final SessionStatePort sessionStatePort;
    private final GameDataPort gameDataPort;

    public ManagerManagementService(SessionStatePort sessionStatePort, GameDataPort gameDataPort) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataPort = gameDataPort;
    }

    @Override
    public List<Manager> getManagers(String sessionId) {
        log.debug("Fetching all managers for session {}", sessionId);
        Session session = sessionStatePort.load(UUID.fromString(sessionId));
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        
        Object root = session.context().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);
        
        List<Manager> managers = new ArrayList<>();
        for (int i = 0; i < managerObjects.size(); i++) {
            managers.add(mapToDomain(managerObjects.get(i), i));
        }
        return managers;
    }

    @Override
    public Optional<Manager> getManager(String sessionId, int managerId) {
        log.debug("Fetching manager {} for session {}", managerId, sessionId);
        Session session = sessionStatePort.load(UUID.fromString(sessionId));
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        
        Object root = session.context().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);
        
        if (managerId >= 0 && managerId < managerObjects.size()) {
            return Optional.of(mapToDomain(managerObjects.get(managerId), managerId));
        }
        return Optional.empty();
    }

    @Override
    public Manager updateManager(String sessionId, int managerId, Manager updateData) {
        log.debug("Updating manager {} for session {}", managerId, sessionId);
        Session session = sessionStatePort.load(UUID.fromString(sessionId));
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        
        Object root = session.context().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);
        
        if (managerId < 0 || managerId >= managerObjects.size()) {
            throw new IllegalArgumentException("Manager not found");
        }
        
        Object managerObj = managerObjects.get(managerId);

        updateData.validate();
        
        try {
            if (updateData.getName() != null) {
                ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_NAME, updateData.getName());
            }
            if (updateData.getConfidenceBoard() != null) {
                ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_BOARD, updateData.getConfidenceBoard());
            }
            if (updateData.getConfidenceFans() != null) {
                ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_FANS, updateData.getConfidenceFans());
            }
        } catch (Exception e) {
            log.warn("Failed to set manager field", e);
        }
        
        // Save the updated state
        sessionStatePort.save(session);
        
        return mapToDomain(managerObj, managerId);
    }
    
    @SuppressWarnings("unchecked")
    private List<Object> getManagerObjectsSafe(Object root) {
        return gameDataPort.getManagers(root);
    }

    private Manager mapToDomain(Object managerObj, int id) {
        Integer confidenceBoard = null;
        Integer confidenceFans = null;
        String name = null;
        Boolean isHuman = null;
        Integer teamId = null;

        try {
            name = (String) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_NAME);
        } catch (Exception e) { log.trace("Field not found", e); }

        try {
            isHuman = (Boolean) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN);
        } catch (Exception e) { log.trace("Field not found", e); }

        try {
            confidenceBoard = (Integer) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_BOARD);
        } catch (Exception e) { log.trace("Field not found", e); }

        try {
            confidenceFans = (Integer) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_FANS);
        } catch (Exception e) { log.trace("Field not found", e); }

        try {
            teamId = (Integer) ReflectionUtils.getFieldValue(managerObj, "nU"); // Used in getHumanTeam
        } catch (Exception e) { log.trace("Field not found", e); }

        return Manager.of(id, name, isHuman, teamId, confidenceBoard, confidenceFans, null, null, null, null);
    }
}
