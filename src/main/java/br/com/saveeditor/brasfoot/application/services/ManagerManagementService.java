package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.service.GameDataService;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManagerManagementService implements GetManagerUseCase, UpdateManagerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManagerManagementService.class);

    private final SessionStatePort sessionStatePort;
    private final GameDataService gameDataService;

    public ManagerManagementService(SessionStatePort sessionStatePort, GameDataService gameDataService) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataService = gameDataService;
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
        try {
            return (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.HUMAN_MANAGERS_LIST);
        } catch (Exception e) {
            log.warn("Could not load managers list from root", e);
            return new ArrayList<>();
        }
    }

    private Manager mapToDomain(Object managerObj, int id) {
        Manager domain = new Manager();
        domain.setId(id);
        
        try {
            domain.setName((String) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_NAME));
        } catch (Exception e) { log.trace("Field not found", e); }
        
        try {
            domain.setIsHuman((Boolean) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN));
        } catch (Exception e) { log.trace("Field not found", e); }
        
        try {
            domain.setConfidenceBoard((Integer) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_BOARD));
        } catch (Exception e) { log.trace("Field not found", e); }
        
        try {
            domain.setConfidenceFans((Integer) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_FANS));
        } catch (Exception e) { log.trace("Field not found", e); }
        
        try {
            domain.setTeamId((Integer) ReflectionUtils.getFieldValue(managerObj, "nU")); // Used in getHumanTeam
        } catch (Exception e) { log.trace("Field not found", e); }

        return domain;
    }
}
