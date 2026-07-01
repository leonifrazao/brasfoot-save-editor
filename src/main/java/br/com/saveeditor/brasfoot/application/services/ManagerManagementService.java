package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.BatchUpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdateManagerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.ManagerTrophy;
import br.com.saveeditor.brasfoot.domain.ManagerTrophyCompetition;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class ManagerManagementService implements GetManagerUseCase, UpdateManagerUseCase, BatchUpdateManagerUseCase {

    private static final Logger log = LoggerFactory.getLogger(ManagerManagementService.class);

    private final GameDataPort gameDataPort;
    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;
    private final BrasfootGameLibraryPort gameLibraryPort;

    public ManagerManagementService(SessionStatePort sessionStatePort, GameDataPort gameDataPort,
                                    SessionResolver sessionResolver, BrasfootGameLibraryPort gameLibraryPort) {
        this.sessionStatePort = sessionStatePort;
        this.gameDataPort = gameDataPort;
        this.sessionResolver = sessionResolver;
        this.gameLibraryPort = gameLibraryPort;
    }

    @Override
    public List<Manager> getManagers(UUID sessionId) {
        log.debug("Fetching all managers for session {}", sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);
        List<ManagerTrophyCompetition> trophyCompetitions = trophyCompetitions(root);

        List<Manager> managers = new ArrayList<>();
        for (int i = 0; i < managerObjects.size(); i++) {
            managers.add(mapToDomain(managerObjects.get(i), i, trophyCompetitions));
        }
        return managers;
    }

    @Override
    public Optional<Manager> getManager(UUID sessionId, int managerId) {
        log.debug("Fetching manager {} for session {}", managerId, sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);

        if (managerId >= 0 && managerId < managerObjects.size()) {
            return Optional.of(mapToDomain(managerObjects.get(managerId), managerId, trophyCompetitions(root)));
        }
        return Optional.empty();
    }

    @Override
    public Manager updateManager(UUID sessionId, int managerId, Manager updateData) {
        log.debug("Updating manager {} for session {}", managerId, sessionId);
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
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
            if (updateData.getIsHuman() != null || updateData.getTeamId() != null) {
                updateManagerAssignment(root, managerObjects, managerId, managerObj, updateData.getTeamId(), updateData.getIsHuman());
            }
            if (updateData.getTrophies() != null) {
                replaceManagerTrophies(root, managerObj, updateData.getTrophies());
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to update manager fields", e);
        }

        // Save the updated state
        sessionStatePort.save(session);

        return mapToDomain(managerObj, managerId, trophyCompetitions(root));
    }

    @Override
    public BatchResponse<Manager> batchUpdateManagers(UUID sessionId, List<ManagerBatchUpdateCommand> commands) {
        log.info("Batch updating {} managers for session {}", commands.size(), sessionId);

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        List<Object> managerObjects = getManagerObjectsSafe(root);
        List<BatchResult<Manager>> results = new ArrayList<>();

        for (int i = 0; i < commands.size(); i++) {
            ManagerBatchUpdateCommand command = commands.get(i);
            int managerId = command.managerId();

            try {
                if (managerId < 0 || managerId >= managerObjects.size()) {
                    results.add(BatchResult.failure(i, "Manager not found with ID: " + managerId));
                    continue;
                }

                Object managerObj = managerObjects.get(managerId);

                try {
                    if (command.name() != null) {
                        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_NAME, command.name());
                    }
                    if (command.confidenceBoard() != null) {
                        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_BOARD, command.confidenceBoard());
                    }
                    if (command.confidenceFans() != null) {
                        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CONFIDENCE_FANS, command.confidenceFans());
                    }
                } catch (Exception e) {
                    log.warn("Failed to set manager field during batch update", e);
                    results.add(BatchResult.failure(i, "Failed to update manager fields: " + e.getMessage()));
                    continue;
                }

                results.add(BatchResult.success(i, mapToDomain(managerObj, managerId, trophyCompetitions(root))));
            } catch (Exception e) {
                log.warn("Batch update failed for item {}", i, e);
                results.add(BatchResult.failure(i, e.getMessage()));
            }
        }

        sessionStatePort.save(session);
        return new BatchResponse<>(results);
    }

    private List<Object> getManagerObjectsSafe(Object root) {
        return gameDataPort.getManagers(root);
    }

    private Manager mapToDomain(Object managerObj, int id) {
        return mapToDomain(managerObj, id, List.of());
    }

    private Manager mapToDomain(Object managerObj, int id, List<ManagerTrophyCompetition> trophyCompetitions) {
        Integer confidenceBoard = null;
        Integer confidenceFans = null;
        String name = null;
        Boolean isHuman = null;
        Integer teamId = null;
        List<ManagerTrophy> trophies = List.of();

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
            teamId = integerField(managerObj, BrasfootConstants.MANAGER_CURRENT_TEAM_ID);
        } catch (Exception e) { log.trace("Field not found", e); }

        try {
            trophies = mapTrophies(managerObj);
        } catch (Exception e) { log.trace("Manager trophies not found", e); }

        return Manager.of(id, name, isHuman, teamId, confidenceBoard, confidenceFans, trophies, trophyCompetitions);
    }

    @SuppressWarnings("unchecked")
    private List<ManagerTrophy> mapTrophies(Object managerObj) throws ReflectiveOperationException {
        List<Object> trophyObjects = (List<Object>) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_TROPHIES);
        if (trophyObjects == null) {
            return List.of();
        }

        List<ManagerTrophy> trophies = new ArrayList<>();
        for (int i = 0; i < trophyObjects.size(); i++) {
            Object trophyObj = trophyObjects.get(i);
            trophies.add(new ManagerTrophy(
                    i,
                    integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_YEAR),
                    integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_TYPE),
                    integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_VARIANT),
                    integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_TEAM_ID),
                    competitionName(trophyObj)
            ));
        }
        return trophies;
    }

    private String competitionName(Object trophyObj) {
        try {
            Object competition = ReflectionUtils.getFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_REFERENCE);
            if (competition == null) {
                return null;
            }
            Object name = getFieldValue(competition, BrasfootConstants.COMPETITION_NAME);
            return name instanceof String value ? value : null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void replaceManagerTrophies(Object root, Object managerObj, List<ManagerTrophy> trophies) throws ReflectiveOperationException {
        List<Object> currentTrophies = (List<Object>) ReflectionUtils.getFieldValue(managerObj, BrasfootConstants.MANAGER_TROPHIES);
        if (currentTrophies == null) {
            currentTrophies = new ArrayList<>();
        }

        List<CompetitionReference> competitionReferences = competitionReferences(root, currentTrophies);
        List<Object> updatedTrophies = new ArrayList<>();
        for (ManagerTrophy trophy : trophies) {
            Object trophyObj = existingTrophy(currentTrophies, trophy.index());
            if (trophyObj == null) {
                trophyObj = newTrophyObject(currentTrophies);
            }
            setTrophyFields(trophyObj, trophy);
            setMatchingCompetitionReference(competitionReferences, trophyObj, trophy);
            updatedTrophies.add(trophyObj);
        }

        currentTrophies.clear();
        currentTrophies.addAll(updatedTrophies);
        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_TROPHIES, currentTrophies);
    }

    private Object existingTrophy(List<Object> currentTrophies, Integer index) {
        if (index == null || index < 0 || index >= currentTrophies.size()) {
            return null;
        }
        return currentTrophies.get(index);
    }

    private Object newTrophyObject(List<Object> currentTrophies) throws ReflectiveOperationException {
        try {
            Class<?> trophyClass = currentTrophies.isEmpty()
                    ? Class.forName("best.ao", true, gameLibraryPort.getClassLoader())
                    : currentTrophies.get(0).getClass();
            return trophyClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Classe de trofeu do Brasfoot nao encontrada", e);
        }
    }

    private void setTrophyFields(Object trophyObj, ManagerTrophy trophy) throws ReflectiveOperationException {
        ReflectionUtils.setFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_YEAR, valueOrZero(trophy.year()));
        ReflectionUtils.setFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_TYPE, valueOrZero(trophy.competitionType()));
        ReflectionUtils.setFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_VARIANT, valueOrZero(trophy.variant()));
        ReflectionUtils.setFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_TEAM_ID, trophy.teamId() == null ? -1 : trophy.teamId());
    }

    private List<CompetitionReference> competitionReferences(Object root, List<Object> currentTrophies) {
        List<CompetitionReference> references = new ArrayList<>();
        addRootCompetitionReferences(references, rootCompetitions(root));
        addCompetitionReferences(references, currentTrophies);
        for (Object manager : getManagerObjectsSafe(root)) {
            try {
                @SuppressWarnings("unchecked")
                List<Object> managerTrophies = (List<Object>) ReflectionUtils.getFieldValue(manager, BrasfootConstants.MANAGER_TROPHIES);
                addCompetitionReferences(references, managerTrophies);
            } catch (ReflectiveOperationException e) {
                log.trace("Unable to read manager trophy references", e);
            }
        }
        return references;
    }

    private void addCompetitionReferences(List<CompetitionReference> references, List<Object> trophyObjects) {
        if (trophyObjects == null) {
            return;
        }
        for (Object trophyObj : trophyObjects) {
            try {
                Object competition = ReflectionUtils.getFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_REFERENCE);
                if (competition == null) {
                    continue;
                }
                Integer competitionType = integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_TYPE);
                Integer variant = integerField(trophyObj, BrasfootConstants.MANAGER_TROPHY_VARIANT);
                if (competitionType != null && variant != null) {
                    references.add(new CompetitionReference(competitionType, variant, competitionName(trophyObj), competition));
                }
            } catch (ReflectiveOperationException e) {
                log.trace("Unable to read trophy competition reference", e);
            }
        }
    }

    private void addRootCompetitionReferences(List<CompetitionReference> references, List<CompetitionObject> competitions) {
        for (CompetitionObject competition : competitions) {
            if (competition.type() == null || competition.variant() == null || competition.competition() == null) {
                continue;
            }
            references.add(new CompetitionReference(competition.type(), competition.variant(), competition.name(), competition.competition()));
        }
    }

    private void setMatchingCompetitionReference(List<CompetitionReference> references, Object trophyObj, ManagerTrophy trophy) throws ReflectiveOperationException {
        Object competition = matchingCompetitionReference(references, trophy);
        if (competition == null) {
            throw new IllegalArgumentException("Competicao do trofeu nao encontrada para tipo "
                    + valueOrZero(trophy.competitionType()) + " e variante " + valueOrZero(trophy.variant())
                    + ". Use tipo/variante de um trofeu que ja exista no save.");
        }
        ReflectionUtils.setFieldValue(trophyObj, BrasfootConstants.MANAGER_TROPHY_COMPETITION_REFERENCE, competition);
    }

    private Object matchingCompetitionReference(List<CompetitionReference> references, ManagerTrophy trophy) {
        int competitionType = valueOrZero(trophy.competitionType());
        int variant = valueOrZero(trophy.variant());
        if (trophy.competitionName() != null && !trophy.competitionName().isBlank()) {
            Object namedCompetition = references.stream()
                    .filter(reference -> reference.competitionType() == competitionType && reference.variant() == variant)
                    .filter(reference -> sameCompetitionName(reference.name(), trophy.competitionName()))
                    .map(CompetitionReference::competition)
                    .findFirst()
                    .orElse(null);
            if (namedCompetition != null) {
                return namedCompetition;
            }
        }
        return references.stream()
                .filter(reference -> reference.competitionType() == competitionType && reference.variant() == variant)
                .map(CompetitionReference::competition)
                .findFirst()
                .orElse(null);
    }

    private List<ManagerTrophyCompetition> trophyCompetitions(Object root) {
        List<ManagerTrophyCompetition> competitions = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        for (CompetitionObject competition : rootCompetitions(root)) {
            if (competition.type() == null || competition.variant() == null || competition.name() == null || competition.name().isBlank()) {
                continue;
            }
            String key = competitionKey(competition.type(), competition.variant(), competition.name());
            if (keys.contains(key)) {
                continue;
            }
            keys.add(key);
            competitions.add(new ManagerTrophyCompetition(competition.type(), competition.variant(), competition.name()));
        }
        return competitions;
    }

    @SuppressWarnings("unchecked")
    private List<CompetitionObject> rootCompetitions(Object root) {
        List<CompetitionObject> competitions = new ArrayList<>();
        List<Object> schedules;
        try {
            schedules = (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.ROOT_SCHEDULES);
        } catch (ReflectiveOperationException e) {
            log.trace("Unable to read root schedules", e);
            return competitions;
        }
        if (schedules == null) {
            addRootCompetitions(competitions, rootCompetitionArray(root));
            return competitions;
        }

        for (Object schedule : schedules) {
            try {
                List<Object> scheduleCompetitions = (List<Object>) ReflectionUtils.getFieldValue(schedule, BrasfootConstants.SCHEDULE_COMPETITIONS);
                addRootCompetitions(competitions, scheduleCompetitions);
            } catch (ReflectiveOperationException e) {
                log.trace("Unable to read schedule competitions", e);
            }
        }
        addRootCompetitions(competitions, rootCompetitionArray(root));
        return competitions;
    }

    private List<Object> rootCompetitionArray(Object root) {
        try {
            Object value = root.getClass().getMethod("bB").invoke(root);
            if (!(value instanceof Object[] competitionArray)) {
                return List.of();
            }
            List<Object> competitions = new ArrayList<>();
            for (Object competition : competitionArray) {
                if (competition != null) {
                    competitions.add(competition);
                }
            }
            return competitions;
        } catch (ReflectiveOperationException e) {
            log.trace("Unable to read root competition array", e);
            return List.of();
        }
    }

    private void addRootCompetitions(List<CompetitionObject> competitions, List<Object> scheduleCompetitions) {
        if (scheduleCompetitions == null) {
            return;
        }
        for (Object competition : scheduleCompetitions) {
            try {
                Integer type = integerFieldInHierarchy(competition, BrasfootConstants.COMPETITION_TYPE);
                Integer variant = trophyVariant(competition, type);
                String name = stringFieldInHierarchy(competition, BrasfootConstants.COMPETITION_NAME);
                competitions.add(new CompetitionObject(type, variant, name, competition));
            } catch (ReflectiveOperationException e) {
                log.trace("Unable to read competition", e);
            }
        }
    }

    private Integer trophyVariant(Object competition, Integer type) throws ReflectiveOperationException {
        Integer variant = integerFieldInHierarchy(competition, BrasfootConstants.COMPETITION_VARIANT);
        if (type == null || (type != 1 && type != 3)) {
            return variant;
        }

        Integer countryOrDivision = invokeIntegerMethod(competition, "ip");
        return countryOrDivision == null || countryOrDivision < 0 ? variant : countryOrDivision;
    }

    private String stringFieldInHierarchy(Object obj, String fieldName) throws ReflectiveOperationException {
        Object value = getFieldValue(obj, fieldName);
        return value instanceof String text ? text : null;
    }

    private Integer integerFieldInHierarchy(Object obj, String fieldName) throws ReflectiveOperationException {
        Object value = getFieldValue(obj, fieldName);
        return value instanceof Integer integer ? integer : null;
    }

    private Object getFieldValue(Object obj, String fieldName) throws ReflectiveOperationException {
        Field field = declaredField(obj.getClass(), fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    private Field declaredField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }

    private Integer invokeIntegerMethod(Object obj, String methodName) {
        try {
            Object value = obj.getClass().getMethod(methodName).invoke(obj);
            return value instanceof Integer integer ? integer : null;
        } catch (ReflectiveOperationException e) {
            log.trace("Unable to invoke competition method {}", methodName, e);
            return null;
        }
    }

    private boolean sameCompetitionName(String left, String right) {
        return normalizeCompetitionName(left).equals(normalizeCompetitionName(right));
    }

    private String competitionKey(Integer type, Integer variant, String name) {
        return type + ":" + variant + ":" + normalizeCompetitionName(name);
    }

    private String normalizeCompetitionName(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private record CompetitionReference(int competitionType, int variant, String name, Object competition) {
    }

    private record CompetitionObject(Integer type, Integer variant, String name, Object competition) {
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void updateManagerAssignment(Object root, List<Object> managerObjects, int managerId, Object managerObj,
                                         Integer targetTeamId, Boolean requestedHuman) throws ReflectiveOperationException {
        boolean human = requestedHuman != null ? requestedHuman : booleanField(managerObj, BrasfootConstants.MANAGER_IS_HUMAN);
        List<Object> teamObjects = gameDataPort.getTeams(root);
        Object currentTeam = findCurrentTeam(teamObjects, managerObj);
        Object targetTeam = targetTeamId == null ? currentTeam : targetTeamId < 0 ? null : findTeamByInternalId(teamObjects, targetTeamId);

        if (targetTeamId != null && targetTeamId >= 0 && targetTeam == null) {
            throw new IllegalArgumentException("Time nao encontrado para transferir tecnico humano: " + targetTeamId);
        }

        if (!human) {
            ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN, false);
            detachManagerFromTeam(root, currentTeam, managerObj);
            setManagerCurrentTeam(managerObj, null);
            return;
        }

        if (targetTeamId != null && targetTeamId < 0) {
            ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN, true);
            detachManagerFromTeam(root, currentTeam, managerObj);
            setManagerCurrentTeam(managerObj, null);
            return;
        }

        if (human && targetTeam == null) {
            ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN, true);
            setManagerCurrentTeam(managerObj, null);
            return;
        }

        int resolvedTargetTeamId = integerFieldRequired(targetTeam, BrasfootConstants.TEAM_ID);
        validateTargetTeamAvailable(managerObjects, managerId, targetTeam, resolvedTargetTeamId, managerObj);

        if (currentTeam != null && currentTeam != targetTeam) {
            detachManagerFromTeam(root, currentTeam, managerObj);
        }

        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_IS_HUMAN, true);
        setManagerCurrentTeam(managerObj, targetTeam);
        attachManagerToTeam(root, targetTeam, managerObj);
    }

    private Object findCurrentTeam(List<Object> teamObjects, Object managerObj) {
        Integer currentTeamId = integerField(managerObj, BrasfootConstants.MANAGER_CURRENT_TEAM_ID);
        if (currentTeamId != null && currentTeamId >= 0) {
            Object team = findTeamByInternalId(teamObjects, currentTeamId);
            if (team != null) {
                return team;
            }
        }

        Integer managerInternalId = integerField(managerObj, BrasfootConstants.MANAGER_ID);
        if (managerInternalId == null) {
            return null;
        }
        return teamObjects.stream()
                .filter(team -> managerInternalId.equals(integerField(team, BrasfootConstants.TEAM_MANAGER_ID)))
                .findFirst()
                .orElse(null);
    }

    private Object findTeamByInternalId(List<Object> teamObjects, int teamId) {
        return teamObjects.stream()
                .filter(team -> Integer.valueOf(teamId).equals(integerField(team, BrasfootConstants.TEAM_ID)))
                .findFirst()
                .orElse(null);
    }

    private void validateTargetTeamAvailable(List<Object> managerObjects, int managerId, Object targetTeam,
                                             int targetTeamId, Object managerObj) {
        for (int i = 0; i < managerObjects.size(); i++) {
            if (i == managerId) {
                continue;
            }
            Object otherManager = managerObjects.get(i);
            if (booleanField(otherManager, BrasfootConstants.MANAGER_IS_HUMAN)
                    && Integer.valueOf(targetTeamId).equals(integerField(otherManager, BrasfootConstants.MANAGER_CURRENT_TEAM_ID))) {
                throw new IllegalArgumentException("Time ja esta controlado por outro tecnico humano");
            }
        }

        Integer currentManagerId = integerField(managerObj, BrasfootConstants.MANAGER_ID);
        Integer targetManagerId = integerField(targetTeam, BrasfootConstants.TEAM_MANAGER_ID);
        if (booleanField(targetTeam, BrasfootConstants.TEAM_IS_HUMAN)
                && targetManagerId != null
                && currentManagerId != null
                && !targetManagerId.equals(currentManagerId)) {
            throw new IllegalArgumentException("Time ja esta marcado como humano por outro tecnico");
        }
    }

    private void attachManagerToTeam(Object root, Object teamObj, Object managerObj) throws ReflectiveOperationException {
        Integer managerInternalId = integerField(managerObj, BrasfootConstants.MANAGER_ID);
        if (managerInternalId == null) {
            throw new IllegalArgumentException("Tecnico sem ID interno no save");
        }
        ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_IS_HUMAN, true);
        ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MANAGER_ID, managerInternalId);
        setFieldIfPresent(teamObj, BrasfootConstants.TEAM_MANAGER_REFERENCE, managerObj);
        addHumanTeam(root, teamObj);
    }

    private void detachManagerFromTeam(Object root, Object teamObj, Object managerObj) throws ReflectiveOperationException {
        if (teamObj == null) {
            return;
        }
        Integer managerInternalId = integerField(managerObj, BrasfootConstants.MANAGER_ID);
        Integer teamManagerId = integerField(teamObj, BrasfootConstants.TEAM_MANAGER_ID);
        if (managerInternalId != null && teamManagerId != null && !managerInternalId.equals(teamManagerId)) {
            return;
        }
        ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_IS_HUMAN, false);
        ReflectionUtils.setFieldValue(teamObj, BrasfootConstants.TEAM_MANAGER_ID, -1);
        setFieldIfPresent(teamObj, BrasfootConstants.TEAM_MANAGER_REFERENCE, null);
        removeHumanTeam(root, teamObj);
    }

    private void setManagerCurrentTeam(Object managerObj, Object teamObj) throws ReflectiveOperationException {
        setFieldIfPresent(managerObj, BrasfootConstants.MANAGER_CURRENT_TEAM, teamObj);
        if (teamObj == null) {
            ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CURRENT_TEAM_ID, -1);
            setFieldIfPresent(managerObj, BrasfootConstants.MANAGER_PREVIOUS_TEAM, null);
            setFieldIfPresent(managerObj, BrasfootConstants.MANAGER_PREVIOUS_TEAM_ID, -1);
            return;
        }
        int teamId = integerFieldRequired(teamObj, BrasfootConstants.TEAM_ID);
        ReflectionUtils.setFieldValue(managerObj, BrasfootConstants.MANAGER_CURRENT_TEAM_ID, teamId);
    }

    @SuppressWarnings("unchecked")
    private void addHumanTeam(Object root, Object teamObj) {
        try {
            List<Object> humanTeams = (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.HUMAN_TEAMS_LIST);
            if (humanTeams != null && !humanTeams.contains(teamObj)) {
                humanTeams.add(teamObj);
            }
        } catch (ReflectiveOperationException e) {
            log.trace("Human teams list not found", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void removeHumanTeam(Object root, Object teamObj) {
        try {
            List<Object> humanTeams = (List<Object>) ReflectionUtils.getFieldValue(root, BrasfootConstants.HUMAN_TEAMS_LIST);
            if (humanTeams != null) {
                humanTeams.remove(teamObj);
            }
        } catch (ReflectiveOperationException e) {
            log.trace("Human teams list not found", e);
        }
    }

    private void setFieldIfPresent(Object target, String fieldName, Object value) throws IllegalAccessException {
        try {
            ReflectionUtils.setFieldValue(target, fieldName, value);
        } catch (NoSuchFieldException e) {
            log.trace("Optional field {} not found", fieldName, e);
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

    private int integerFieldRequired(Object obj, String fieldName) throws ReflectiveOperationException {
        Object value = ReflectionUtils.getFieldValue(obj, fieldName);
        if (value == null) {
            throw new IllegalArgumentException("Campo obrigatorio ausente: " + fieldName);
        }
        return ((Number) value).intValue();
    }

    private boolean booleanField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return Boolean.TRUE.equals(value);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
