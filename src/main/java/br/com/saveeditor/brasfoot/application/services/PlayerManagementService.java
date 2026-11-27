package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.GetPlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UpdatePlayerUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerTransferTerms;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.BatchResult;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.PlayerExtraAttributes;
import br.com.saveeditor.brasfoot.domain.enums.PlayerSide;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.IdentityHashMap;
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
        var globalIndices = new IdentityHashMap<Object, Integer>();
        List<Object> globalPlayers = optionalList(root, BrasfootConstants.SENIOR_PLAYERS_LIST);
        for (int i = 0; i < globalPlayers.size(); i++) {
            globalIndices.putIfAbsent(globalPlayers.get(i), i);
        }

        int unregisteredIndex = 0;
        for (int i = 0; i < playerObjects.size(); i++) {
            Object object = playerObjects.get(i);
            Player player = mapToPlayerDomain(sessionId, teamId, object, i);
            Integer globalIndex = globalIndices.get(object);
            player.setDraftKey(globalIndex == null ? unregisteredIndex++ : -1 - globalIndex);
            players.add(player);
        }

        return players;
    }

    public Player transferPlayer(UUID sessionId, int fromId, int playerId, int toId) {
        return transferPlayer(sessionId, fromId, playerId, toId, PlayerTransferTerms.DEFAULT).player();
    }

    public CompletedTransfer transferPlayer(UUID sessionId, int fromId, int playerId, int toId, PlayerTransferTerms terms) {
        if (fromId == toId) throw new IllegalArgumentException("Escolha outro time de destino.");

        Session session = sessionResolver.loadRequired(sessionId);
        Object root = session.getContext().getState().getObjetoRaiz();
        Object from = gameDataPort.getTeamById(root, fromId);
        Object to = gameDataPort.getTeamById(root, toId);
        if (from == null || to == null) throw new IllegalArgumentException("Time de origem ou destino não encontrado.");

        List<Object> origin = gameDataPort.getPlayers(from);
        List<Object> destination = gameDataPort.getPlayers(to);
        if (playerId < 0 || playerId >= origin.size()) throw new IllegalArgumentException("Jogador não encontrado no elenco de origem.");
        Object player = origin.get(playerId);

        try {
            if (!Integer.valueOf(fromId).equals(ReflectionUtils.getFieldValue(player, BrasfootConstants.PLAYER_CLUB_ID))
                    || destination.stream().anyMatch(member -> member == player)) {
                throw new IllegalArgumentException("O vínculo do jogador com o clube está inconsistente no save.");
            }
            if (Boolean.TRUE.equals(ReflectionUtils.getFieldValue(player, BrasfootConstants.PLAYER_ON_LOAN))) {
                throw new IllegalArgumentException("O jogador está emprestado. Encerre o empréstimo no jogo antes de transferi-lo.");
            }

            Player result = mapToPlayerDomain(sessionId, fromId, player, playerId);
            List<Object> transfers = listField(root, BrasfootConstants.PLAYER_TRANSFERS_LIST);
            Calendar gameCalendar = (Calendar) root.getClass().getMethod("bb").invoke(root);
            LocalDate date = terms.date() != null ? terms.date() : LocalDate.of(gameCalendar.get(Calendar.YEAR),
                    gameCalendar.get(Calendar.MONTH) + 1, gameCalendar.get(Calendar.DAY_OF_MONTH));
            if (terms.contractEnd() != null && terms.contractEnd().isBefore(date)) {
                throw new IllegalArgumentException("O fim do contrato deve ser na data da transferência ou depois.");
            }
            Class<?> transferType = root.getClass().getClassLoader().loadClass("best.ap");
            Object transfer = transferType.getConstructor().newInstance();
            transferType.getMethod("f", int.class, int.class, int.class).invoke(transfer,
                    date.getDayOfMonth(), date.getMonthValue() - 1, date.getYear());
            ReflectionUtils.setFieldValue(transfer, "U", player);
            ReflectionUtils.setFieldValue(transfer, "oo", fromId);
            ReflectionUtils.setFieldValue(transfer, "op", toId);
            ReflectionUtils.setFieldValue(transfer, "oq", terms.fee());
            transferType.getMethod("mb").invoke(transfer); // Persist the player numbers for relinking on game load.
            int historyIndex = transferHistoryIndex(transfers, date);

            if (terms.fee() > 0) {
                recordTransferPayment(from, terms.fee(), "dN");
                recordTransferPayment(to, -terms.fee(), "dR");
            }
            if (terms.salary() != null) {
                ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_SALARY, terms.salary());
            }
            if (terms.contractEnd() != null) {
                long timestamp = terms.contractEnd().atStartOfDay(gameCalendar.getTimeZone().toZoneId())
                        .toInstant().toEpochMilli();
                ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_CONTRACT_END, timestamp);
            }

            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_PREVIOUS_CLUB, fromId);
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_CLUB_ID, toId);
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_CLUB_REFERENCE, to);
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_SHIRT_NUMBER, 0);
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_LISTED_FOR_LOAN, false);
            ReflectionUtils.setFieldValue(player, BrasfootConstants.PLAYER_LISTED_FOR_SALE, false);
            removeFromLineups(from, player);
            ReflectionUtils.setFieldValue(to, BrasfootConstants.TEAM_LINEUP_CONFIRMED, false);
            origin.removeIf(member -> member == player);
            destination.add(player);
            transfers.add(historyIndex, transfer);
            sessionStatePort.save(session);
            return new CompletedTransfer(result, date);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Falha ao transferir o jogador: " + rootCauseMessage(e), e);
        }
    }

    public record CompletedTransfer(Player player, LocalDate date) {}

    private void recordTransferPayment(Object team, int cashChange, String financeField) throws ReflectiveOperationException {
        long balance = (long) ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_MONEY);
        ReflectionUtils.setFieldValue(team, BrasfootConstants.TEAM_MONEY, Math.addExact(balance, cashChange));
        if (Boolean.TRUE.equals(ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_IS_HUMAN))) {
            Object finances = ReflectionUtils.getFieldValue(team, "nx");
            if (finances == null) throw new IllegalStateException("O clube controlado por jogador não tem dados financeiros no save.");
            long total = (long) ReflectionUtils.getFieldValue(finances, financeField);
            ReflectionUtils.setFieldValue(finances, financeField, Math.addExact(total, Math.abs((long) cashChange)));
        }
    }

    private int transferHistoryIndex(List<Object> history, LocalDate date) throws ReflectiveOperationException {
        long dateKey = date.getYear() * 10_000L + date.getMonthValue() * 100L + date.getDayOfMonth();
        for (int i = history.size() - 1; i >= 0; i--) {
            Object transfer = history.get(i);
            long year = (int) ReflectionUtils.getFieldValue(transfer, "y");
            int month = (int) ReflectionUtils.getFieldValue(transfer, "on");
            int day = (int) ReflectionUtils.getFieldValue(transfer, "d");
            if (year * 10_000L + month * 100L + day <= dateKey) return i + 1;
        }
        return 0;
    }

    private void removeFromLineups(Object team, Object player) throws ReflectiveOperationException {
        ReflectionUtils.setFieldValue(team, BrasfootConstants.TEAM_LINEUP_CONFIRMED, false);
        for (String field : List.of("nf", "ng")) { // Last starters and substitutes.
            listField(team, field).removeIf(member -> member == player);
        }
        for (String field : List.of("nh", "ni", "nj", "nk")) { // Captain and set-piece takers.
            if (ReflectionUtils.getFieldValue(team, field) == player) ReflectionUtils.setFieldValue(team, field, null);
        }
        for (Object lineup : listField(team, "ny")) clearLineupSlot(lineup, player);
        clearLineupSlot(ReflectionUtils.getFieldValue(team, "nz"), player);
    }

    private void clearLineupSlot(Object lineup, Object player) throws ReflectiveOperationException {
        if (lineup != null) Collections.replaceAll(listField(lineup, "vp"), player, null);
    }

    private List<Object> optionalList(Object object, String field) {
        try {
            return listField(object, field);
        } catch (ReflectiveOperationException e) {
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> listField(Object object, String field) throws ReflectiveOperationException {
        Object value = ReflectionUtils.getFieldValue(object, field);
        return value == null ? Collections.emptyList() : (List<Object>) value;
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
            validateCoreUpdate(mapToPlayerDomain(sessionId, teamId, playerObj, playerId), age, overall, position, energy);
            if (age != null) {
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_AGE, age);
            }
            if (overall != null) {
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_OVERALL, overall);
            }
            if (position != null) {
                ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION, position);
            }
            if (energy != null) {
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
        return updatePlayer(sessionId, teamId, playerId, name, age, overall, position, energy, salary, side, contractEnd,
                characteristic1, characteristic2, skillGoalkeeping, skillSpeed, skillTechnique, skillPassing,
                skillTackling, skillPlaymaking, skillFinishing, country, starLocal, starGlobal, PlayerExtraAttributes.EMPTY);
    }

    public Player updatePlayer(UUID sessionId, int teamId, int playerId, String name, Integer age, Integer overall,
                               Integer position, Integer energy, Integer salary, Integer side, Long contractEnd,
                               Integer characteristic1, Integer characteristic2, Integer skillGoalkeeping,
                               Integer skillSpeed, Integer skillTechnique, Integer skillPassing, Integer skillTackling,
                               Integer skillPlaymaking, Integer skillFinishing, Integer country,
                               Boolean starLocal, Boolean starGlobal, PlayerExtraAttributes extra) {
        extra.validateUpdate();
        if (side != null && PlayerSide.fromCode(side).getCode() != side) {
            throw new IllegalArgumentException("Lado inválido. Use 0 para direito ou 1 para esquerdo.");
        }
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
            setIfPresent(playerObj, BrasfootConstants.PLAYER_POTENTIAL, extra.potential());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_ESTIMATED_POTENTIAL, extra.estimatedPotential());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_DEVELOPMENT, extra.development());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_SHIRT_NUMBER, extra.shirtNumber());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_MARKET_VALUE, extra.marketValue());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_ASKING_PRICE, extra.askingPrice());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_LISTED_FOR_SALE, extra.listedForSale());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_LISTED_FOR_LOAN, extra.listedForLoan());
            setIfPresent(playerObj, BrasfootConstants.PLAYER_INJURED_UNTIL, extra.injuredUntil());
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
                validateCoreUpdate(mapToPlayerDomain(sessionId, teamId, playerObj, playerId),
                        command.age(), command.overall(), command.position(), command.energy());
                if (command.age() != null) {
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_AGE, command.age());
                }
                if (command.overall() != null) {
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
                    ReflectionUtils.setFieldValue(playerObj, BrasfootConstants.PLAYER_POSITION, command.position());
                }
                if (command.energy() != null) {
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

    private void validateCoreUpdate(Player player, Integer age, Integer overall, Integer position, Integer energy) {
        if (age != null) player.setAge(age);
        if (overall != null) player.setOverall(overall);
        if (position != null) player.setPosition(position);
        if (energy != null) player.setEnergy(energy);
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
            player.setExtraAttributes(new PlayerExtraAttributes(integerField(playerObj, BrasfootConstants.PLAYER_POTENTIAL),
                    integerField(playerObj, BrasfootConstants.PLAYER_ESTIMATED_POTENTIAL),
                    integerField(playerObj, BrasfootConstants.PLAYER_DEVELOPMENT),
                    integerField(playerObj, BrasfootConstants.PLAYER_SHIRT_NUMBER),
                    integerField(playerObj, BrasfootConstants.PLAYER_MARKET_VALUE),
                    integerField(playerObj, BrasfootConstants.PLAYER_ASKING_PRICE),
                    optionalBoolean(playerObj, BrasfootConstants.PLAYER_LISTED_FOR_SALE),
                    optionalBoolean(playerObj, BrasfootConstants.PLAYER_LISTED_FOR_LOAN),
                    longField(playerObj, BrasfootConstants.PLAYER_INJURED_UNTIL)));
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

    private Boolean optionalBoolean(Object obj, String fieldName) {
        try {
            return Boolean.TRUE.equals(ReflectionUtils.getFieldValue(obj, fieldName));
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
