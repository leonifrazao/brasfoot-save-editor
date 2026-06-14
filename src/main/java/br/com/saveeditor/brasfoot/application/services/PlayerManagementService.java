package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
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

import static net.logstash.logback.argument.StructuredArguments.kv;

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
        log.debug("player_list_requested {} {}", kv("session_id", sessionId), kv("team_id", teamId));
        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        log.info("player_list_loaded {} {} {} {}",
                kv("session_id", sessionId),
                kv("team_id", teamId),
                kv("player_count", playerObjects.size()),
                kv("team_class", teamObj.getClass().getName()));
        List<Player> players = new ArrayList<>();

        for (int i = 0; i < playerObjects.size(); i++) {
            players.add(mapToPlayerDomain(sessionId, teamId, playerObjects.get(i), i));
        }

        return players;
    }

    @Override
    public Player getPlayer(UUID sessionId, int teamId, int playerId) {
        log.debug("player_requested {} {} {}", kv("session_id", sessionId), kv("team_id", teamId), kv("player_id", playerId));
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

        return mapToPlayerDomain(sessionId, teamId, playerObjects.get(playerId), playerId);
    }

    @Override
    public Player updatePlayer(UUID sessionId, int teamId, int playerId, Integer age, Integer overall, Integer position, Integer energy, Integer morale, Boolean starLocal, Boolean starGlobal) {
        log.info("player_update_requested {} {} {}", kv("session_id", sessionId), kv("team_id", teamId), kv("player_id", playerId));
        log.debug("player_update_payload {} {} {} {} {} {} {} {} {} {}",
                kv("session_id", sessionId), kv("team_id", teamId), kv("player_id", playerId),
                kv("age", age), kv("overall", overall), kv("position", position), kv("energy", energy),
                kv("morale", morale), kv("star_local", starLocal), kv("star_global", starGlobal));

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
            Player currentPlayer = mapToPlayerDomain(sessionId, teamId, playerObj, playerId);

            if (age != null) {
                Player.builder()
                        .id(currentPlayer.getId())
                        .name(currentPlayer.getName())
                        .age(age)
                        .overall(currentPlayer.getOverall())
                        .position(currentPlayer.getPosition())
                        .energy(currentPlayer.getEnergy())
                        .morale(currentPlayer.getMorale())
                        .starLocal(currentPlayer.isStarLocal())
                        .starGlobal(currentPlayer.isStarGlobal())
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
                        .starLocal(currentPlayer.isStarLocal())
                        .starGlobal(currentPlayer.isStarGlobal())
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
                        .starLocal(currentPlayer.isStarLocal())
                        .starGlobal(currentPlayer.isStarGlobal())
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
                        .starLocal(currentPlayer.isStarLocal())
                        .starGlobal(currentPlayer.isStarGlobal())
                        .build();
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_ENERGY, energy);
            }
            if (starLocal != null) {
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_STAR_LOCAL, starLocal);
            }
            if (starGlobal != null) {
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_STAR_GLOBAL, starGlobal);
            }

        } catch (IllegalArgumentException e) {
            log.warn("player_update_validation_failed {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                    kv("player_id", playerId), kv("error", e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.error("player_update_failed {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                    kv("player_id", playerId), kv("error", rootCauseMessage(e)), e);
            throw new RuntimeException("Falha ao atualizar jogador " + playerId + " do time " + teamId + ": " + rootCauseMessage(e), e);
        }

        sessionStatePort.save(session);
        return mapToPlayerDomain(sessionId, teamId, playerObj, playerId);
    }

    public Player updatePlayer(UUID sessionId, int teamId, int playerId, String name, Integer age, Integer overall,
                                Integer position, Integer energy, Integer salary, Integer side, Long contractEnd,
                                Integer characteristic1, Integer characteristic2, Integer skillGoalkeeping,
                                Integer skillSpeed, Integer skillTechnique, Integer skillPassing, Integer skillTackling,
                                Integer skillPlaymaking, Integer skillFinishing, Integer country,
                                Boolean starLocal, Boolean starGlobal) {
        updatePlayer(sessionId, teamId, playerId, age, overall, position, energy, null, starLocal, starGlobal);
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
            setIfPresent(playerObj, BrasfootConstants.PLAYER_NAME, name);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SALARY, salary);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SIDE, side);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_CONTRACT_END, contractEnd);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_CHARACTERISTIC_1, characteristic1);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_CHARACTERISTIC_2, characteristic2);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_GOALKEEPING, skillGoalkeeping);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_SPEED, skillSpeed);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_TECHNIQUE, skillTechnique);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_PASSING, skillPassing);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_TACKLING, skillTackling);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_PLAYMAKING, skillPlaymaking);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_FINISHING, skillFinishing);
            setIfPresent(playerObj, BrasfootConstants.PLAYER_PAIS, country);
            sessionStatePort.save(session);
            log.info("player_extended_update_applied {} {} {}", kv("session_id", sessionId), kv("team_id", teamId), kv("player_id", playerId));
            return mapToPlayerDomain(sessionId, teamId, playerObj, playerId);
        } catch (Exception e) {
            log.error("player_extended_update_failed {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                    kv("player_id", playerId), kv("error", rootCauseMessage(e)), e);
            throw new RuntimeException("Falha ao atualizar campos extras do jogador " + playerId + " do time " + teamId + ": " + rootCauseMessage(e), e);
        }
    }

    @Override
    public BatchResponse<Player> batchUpdatePlayers(UUID sessionId, int teamId, List<PlayerBatchUpdateCommand> commands) {
        log.info("player_batch_update_requested {} {} {}", kv("session_id", sessionId), kv("team_id", teamId), kv("command_count", commands.size()));

        Session session = sessionResolver.loadRequired(sessionId);

        Object root = session.getContext().getState().getObjetoRaiz();
        Object teamObj = gameDataPort.getTeamById(root, teamId);
        if (teamObj == null) {
            throw new IllegalArgumentException("Team not found with ID: " + teamId);
        }

        List<Object> playerObjects = gameDataPort.getPlayers(teamObj);
        List<BatchResult<Player>> results = new ArrayList<>();

        for (int i = 0; i < commands.size(); i++) {
            var command = commands.get(i);
            int playerId = command.playerId();
            if (playerId < 0 || playerId >= playerObjects.size()) {
                log.warn("Player index {} out of bounds for team {}", playerId, teamId);
                results.add(BatchResult.failure(i, "Player index out of bounds"));
                continue;
            }

            Object playerObj = playerObjects.get(playerId);

            try {
                Player currentPlayer = mapToPlayerDomain(sessionId, teamId, playerObj, playerId);

                if (command.age() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(command.age())
                            .overall(currentPlayer.getOverall())
                            .position(currentPlayer.getPosition())
                            .energy(currentPlayer.getEnergy())
                            .morale(currentPlayer.getMorale())
                            .starLocal(currentPlayer.isStarLocal())
                            .starGlobal(currentPlayer.isStarGlobal())
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
                            .starLocal(currentPlayer.isStarLocal())
                            .starGlobal(currentPlayer.isStarGlobal())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL, command.overall());
                }
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_GOALKEEPING, command.skillGoalkeeping());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_SPEED, command.skillSpeed());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_TECHNIQUE, command.skillTechnique());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_PASSING, command.skillPassing());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_TACKLING, command.skillTackling());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_PLAYMAKING, command.skillPlaymaking());
                setIfPresent(playerObj, BrasfootConstants.PLAYER_SKILL_FINISHING, command.skillFinishing());
                if (command.position() != null) {
                    Player.builder()
                            .id(currentPlayer.getId())
                            .name(currentPlayer.getName())
                            .age(currentPlayer.getAge())
                            .overall(currentPlayer.getOverall())
                            .position(command.position())
                            .energy(currentPlayer.getEnergy())
                            .morale(currentPlayer.getMorale())
                            .starLocal(currentPlayer.isStarLocal())
                            .starGlobal(currentPlayer.isStarGlobal())
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
                            .starLocal(currentPlayer.isStarLocal())
                            .starGlobal(currentPlayer.isStarGlobal())
                            .build();
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_ENERGY, command.energy());
                }
                if (command.starLocal() != null) {
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_STAR_LOCAL, command.starLocal());
                }
                if (command.starGlobal() != null) {
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_STAR_GLOBAL, command.starGlobal());
                }
                if (command.country() != null) {
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_PAIS, command.country());
                }
            } catch (IllegalArgumentException e) {
                log.warn("player_batch_update_validation_failed {} {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                        kv("player_id", playerId), kv("batch_index", i), kv("error", e.getMessage()));
                results.add(BatchResult.failure(i, e.getMessage()));
                continue;
            } catch (Exception e) {
                log.error("player_batch_update_failed {} {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                        kv("player_id", playerId), kv("batch_index", i), kv("error", rootCauseMessage(e)), e);
                results.add(BatchResult.failure(i, "Failed to update player properties: " + e.getMessage()));
                continue;
            }

            results.add(BatchResult.success(i, mapToPlayerDomain(sessionId, teamId, playerObj, playerId)));
        }

        sessionStatePort.save(session);
        return new BatchResponse<>(results);
    }

    private Player mapToPlayerDomain(UUID sessionId, int teamId, Object playerObj, int index) {
        String playerClass = playerObj == null ? "null" : playerObj.getClass().getName();
        try {
            String name = requiredStringField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_NAME, "name");
            int age = requiredIntField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_AGE, "age");
            int overall = requiredIntField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_OVERALL, "overall");
            int position = requiredIntField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_POSITION, "position");
            int energy = requiredIntField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_ENERGY, "energy");
            boolean starLocal = requiredBooleanField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_STAR_LOCAL, "starLocal");
            boolean starGlobal = requiredBooleanField(sessionId, teamId, index, playerObj, BrasfootConstants.PLAYER_STAR_GLOBAL, "starGlobal");

            // Assume default 100 for morale for now
            int morale = 100;

            Player player = new Player(index, name, age, overall, position, energy, morale, starLocal, starGlobal);
            player.setSalary(integerField(playerObj, BrasfootConstants.PLAYER_SALARY));
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SIDE, "side", integerField(playerObj, BrasfootConstants.PLAYER_SIDE), player::setSide);
            player.setContractEnd(longField(playerObj, BrasfootConstants.PLAYER_CONTRACT_END));
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_PAIS, "country", integerField(playerObj, BrasfootConstants.PLAYER_PAIS), player::setCountry);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_CHARACTERISTIC_1, "characteristic1", integerField(playerObj, BrasfootConstants.PLAYER_CHARACTERISTIC_1), player::setCharacteristic1);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_CHARACTERISTIC_2, "characteristic2", integerField(playerObj, BrasfootConstants.PLAYER_CHARACTERISTIC_2), player::setCharacteristic2);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_GOALKEEPING, "skillGoalkeeping", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_GOALKEEPING), player::setSkillGoalkeeping);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_SPEED, "skillSpeed", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_SPEED), player::setSkillSpeed);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_TECHNIQUE, "skillTechnique", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_TECHNIQUE), player::setSkillTechnique);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_PASSING, "skillPassing", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_PASSING), player::setSkillPassing);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_TACKLING, "skillTackling", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_TACKLING), player::setSkillTackling);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_PLAYMAKING, "skillPlaymaking", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_PLAYMAKING), player::setSkillPlaymaking);
            setOptionalPlayerField(sessionId, teamId, index, playerClass, BrasfootConstants.PLAYER_SKILL_FINISHING, "skillFinishing", integerField(playerObj, BrasfootConstants.PLAYER_SKILL_FINISHING), player::setSkillFinishing);

            log.debug("player_mapped {} {} {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                    kv("player_id", index), kv("player_name", name), kv("player_class", playerClass), kv("field_count", 19));
            return player;
        } catch (Exception e) {
            log.error("player_mapping_failed {} {} {} {} {}", kv("session_id", sessionId), kv("team_id", teamId),
                    kv("player_id", index), kv("player_class", playerClass), kv("error", rootCauseMessage(e)), e);
            throw new IllegalStateException("Falha ao mapear jogador #" + index + " do time " + teamId + ": " + rootCauseMessage(e), e);
        }
    }

    @FunctionalInterface
    private interface IntegerSetter {
        void set(Integer value);
    }

    private void setOptionalPlayerField(UUID sessionId, int teamId, int playerId, String playerClass, String obfuscatedField,
                                        String logicalField, Integer value, IntegerSetter setter) {
        try {
            setter.set(value);
        } catch (IllegalArgumentException e) {
            log.warn("player_optional_field_ignored {} {} {} {} {} {} {} {}", kv("session_id", sessionId),
                    kv("team_id", teamId), kv("player_id", playerId), kv("player_class", playerClass),
                    kv("field", logicalField), kv("obfuscated_field", obfuscatedField), kv("value", value),
                    kv("error", e.getMessage()));
        }
    }

    private String requiredStringField(UUID sessionId, int teamId, int playerId, Object obj, String obfuscatedField, String logicalField) {
        Object value = requiredField(sessionId, teamId, playerId, obj, obfuscatedField, logicalField);
        if (value instanceof String text) {
            return text;
        }
        throw invalidFieldType(obfuscatedField, logicalField, value, "String");
    }

    private int requiredIntField(UUID sessionId, int teamId, int playerId, Object obj, String obfuscatedField, String logicalField) {
        Object value = requiredField(sessionId, teamId, playerId, obj, obfuscatedField, logicalField);
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw invalidFieldType(obfuscatedField, logicalField, value, "Number");
    }

    private boolean requiredBooleanField(UUID sessionId, int teamId, int playerId, Object obj, String obfuscatedField, String logicalField) {
        Object value = requiredField(sessionId, teamId, playerId, obj, obfuscatedField, logicalField);
        if (value instanceof Boolean bool) {
            return bool;
        }
        throw invalidFieldType(obfuscatedField, logicalField, value, "Boolean");
    }

    private Object requiredField(UUID sessionId, int teamId, int playerId, Object obj, String obfuscatedField, String logicalField) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, obfuscatedField);
            if (value == null) {
                throw new IllegalStateException("Campo obrigatorio nulo: " + logicalField + " (" + obfuscatedField + ")");
            }
            return value;
        } catch (ReflectiveOperationException e) {
            log.error("player_required_field_read_failed {} {} {} {} {} {}", kv("session_id", sessionId),
                    kv("team_id", teamId), kv("player_id", playerId), kv("field", logicalField),
                    kv("obfuscated_field", obfuscatedField), kv("error", e.getMessage()), e);
            throw new IllegalStateException("Campo obrigatorio ausente: " + logicalField + " (" + obfuscatedField + ")", e);
        }
    }

    private IllegalStateException invalidFieldType(String obfuscatedField, String logicalField, Object value, String expectedType) {
        String actualType = value == null ? "null" : value.getClass().getName();
        return new IllegalStateException("Tipo invalido em " + logicalField + " (" + obfuscatedField + "): esperado "
                + expectedType + ", recebido " + actualType + ", valor=" + value);
    }

    private void setIfPresent(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        if (value != null) {
            ReflectionUtils.setFieldValue(target, fieldName, value);
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

    private Long longField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return value == null ? null : ((Number) value).longValue();
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }
}
