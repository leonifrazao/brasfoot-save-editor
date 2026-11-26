package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.presentation.model.CalendarRoundRow;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class CalendarManagementService {
    private static final String CALENDAR_FIELD = "as";
    private static final String MATCH_DAY_OF_YEAR_FIELD = "fy";
    private static final String CALENDAR_DAY_TYPE_FIELD = "w";
    private static final int HUMAN_MATCH_DAY_TYPE = 200;
    private static final String TEAM_IS_HUMAN_FIELD = "mW";
    private static final String POINTS_STAGE_CLASS = "f.s";

    private final SessionResolver sessionResolver;
    private final SessionStatePort sessionStatePort;

    public CalendarManagementService(SessionResolver sessionResolver, SessionStatePort sessionStatePort) {
        this.sessionResolver = sessionResolver;
        this.sessionStatePort = sessionStatePort;
    }

    public List<CalendarRoundRow> getUpcomingRounds(UUID sessionId) {
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        LocalDate currentDate = currentDate(root);
        Map<GroupKey, RoundGroup> rounds = collectRounds(root);
        Map<String, List<RoundGroup>> byCompetition = new LinkedHashMap<>();
        rounds.values().stream().sorted(Comparator.comparing((RoundGroup round) -> round.date)
                        .thenComparing(round -> round.competition))
                .forEach(round -> byCompetition.computeIfAbsent(round.competitionKey, ignored -> new ArrayList<>()).add(round));

        List<CalendarRoundRow> result = new ArrayList<>();
        for (List<RoundGroup> competitionRounds : byCompetition.values()) {
            for (int index = 0; index < competitionRounds.size(); index++) {
                RoundGroup round = competitionRounds.get(index);
                if (round.date.isAfter(currentDate)) {
                    result.add(round.toRow("Rodada " + (index + 1)));
                }
            }
        }
        return result.stream().sorted(Comparator.comparing(CalendarRoundRow::date)
                .thenComparing(CalendarRoundRow::competition)).toList();
    }

    public int moveRound(UUID sessionId, String competitionKey, LocalDate sourceDate, LocalDate targetDate) {
        Objects.requireNonNull(sourceDate, "Data atual da rodada obrigatoria");
        Objects.requireNonNull(targetDate, "Nova data obrigatoria");
        if (sourceDate.equals(targetDate)) {
            throw new IllegalArgumentException("Escolha uma data diferente da data atual da rodada.");
        }

        Session session = sessionResolver.loadRequired(sessionId);
        Object root = session.getContext().getState().getObjetoRaiz();
        LocalDate currentDate = currentDate(root);
        if (!sourceDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("So e possivel mover rodadas futuras. A data atual do save e " + currentDate + ".");
        }
        if (!targetDate.isAfter(currentDate)) {
            throw new IllegalArgumentException("A nova data precisa ser posterior a data atual do save (" + currentDate + ").");
        }

        List<Object> days = calendarDays(root);
        Object sourceDay = findDay(days, sourceDate);
        Object targetDay = findDay(days, targetDate);
        if (sourceDay == null || targetDay == null) {
            throw new IllegalArgumentException("Nao encontrei um dia de calendario para a data escolhida.");
        }

        List<Object> sourceMatches = list(call(sourceDay, "h"));
        List<Object> moving = sourceMatches.stream()
                .filter(match -> competitionKey(match).equals(competitionKey)
                        && isPointsStage(call(match, "ht")))
                .toList();
        if (moving.isEmpty()) {
            throw new IllegalArgumentException("A rodada selecionada nao esta mais nessa data. Atualize o calendario e tente novamente.");
        }

        validateRoundOrder(days, competitionKey, sourceDate, targetDate);
        validateNoClubCollision(targetDay, moving, competitionKey);

        List<Object> destinationMatches = list(call(targetDay, "h"));
        for (Object match : moving) {
            sourceMatches.remove(match);
            updateMatchDayOfYear(match, targetDate);
            destinationMatches.add(match);
            invokeCompatible(targetDay, "a", call(match, "ht"));
            invokeCompatible(targetDay, "a", call(match, "hy"));
        }

        pruneDayMetadata(sourceDay, sourceMatches);
        updateCalendarDayType(sourceDay, sourceMatches);
        moveHumanMatchesFirst(destinationMatches);
        updateCalendarDayType(targetDay, destinationMatches);
        sessionStatePort.save(session);
        return moving.size();
    }

    public boolean hasHumanNationalTournamentConflict(UUID sessionId, String competitionKey,
                                                      LocalDate sourceDate, LocalDate targetDate) {
        Objects.requireNonNull(sourceDate, "Data atual da rodada obrigatoria");
        Objects.requireNonNull(targetDate, "Nova data obrigatoria");
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        List<Object> days = calendarDays(root);
        Object sourceDay = findDay(days, sourceDate);
        Object targetDay = findDay(days, targetDate);
        if (sourceDay == null || targetDay == null) return false;

        List<Object> moving = list(call(sourceDay, "h")).stream()
                .filter(match -> competitionKey(match).equals(competitionKey)
                        && isPointsStage(call(match, "ht")))
                .toList();
        boolean movesHumanClubMatch = moving.stream().anyMatch(match ->
                isHumanClub(root, call(match, "hc")) || isHumanClub(root, call(match, "hd")));
        if (!movesHumanClubMatch) return false;

        return list(call(targetDay, "h")).stream().anyMatch(match -> {
            Object competition = call(match, "hy");
            if (!isNationalTournament(competitionName(competition))) return false;
            return isHumanNationalTeam(root, call(match, "hc"))
                    || isHumanNationalTeam(root, call(match, "hd"));
        });
    }

    private Map<GroupKey, RoundGroup> collectRounds(Object root) {
        Map<GroupKey, RoundGroup> groups = new LinkedHashMap<>();
        for (Object day : calendarDays(root)) {
            LocalDate date = dayDate(day);
            for (Object match : list(call(day, "h"))) {
                Object competition = call(match, "hy");
                Object stage = call(match, "ht");
                if (!isPointsStage(stage)) continue;
                String label = competitionName(competition);
                String key = competitionObjectKey(competition);
                GroupKey groupKey = new GroupKey(key, date);
                RoundGroup group = groups.computeIfAbsent(groupKey,
                        ignored -> new RoundGroup(key, date, label));
                group.matchCount++;
            }
        }
        return groups;
    }

    private boolean isPointsStage(Object stage) {
        return POINTS_STAGE_CLASS.equals(stage.getClass().getName());
    }

    private void updateMatchDayOfYear(Object match, LocalDate date) {
        try {
            // No Brasfoot original, fy guarda o dia do ano com base zero.
            ReflectionUtils.setFieldValue(match, MATCH_DAY_OF_YEAR_FIELD, date.getDayOfYear() - 1);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel atualizar a data gravada na partida do Brasfoot.", e);
        }
    }

    private void updateCalendarDayType(Object day, List<Object> matches) {
        if (matches.isEmpty()) {
            setCalendarDayType(day, 0);
            return;
        }

        boolean hasHumanManagedMatch = matches.stream().anyMatch(this::hasHumanManagedTeam);
        if (hasHumanManagedMatch) {
            setCalendarDayType(day, HUMAN_MATCH_DAY_TYPE);
            return;
        }

        try {
            Object currentType = call(day, "b");
            if (!(currentType instanceof Number number) || number.intValue() <= 0) {
                setCalendarDayType(day, ((Number) call(call(matches.get(0), "hy"), "b")).intValue());
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private boolean hasHumanManagedTeam(Object match) {
        return isHumanManagedTeam(call(match, "hc")) || isHumanManagedTeam(call(match, "hd"));
    }

    private boolean isHumanManagedTeam(Object team) {
        try {
            return Boolean.TRUE.equals(ReflectionUtils.getFieldValue(team, TEAM_IS_HUMAN_FIELD));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel identificar o time controlado pelo tecnico humano.", e);
        }
    }

    private boolean isHumanClub(Object root, Object team) {
        return isHumanManagedTeam(team) && !isNationalTeam(root, team);
    }

    private boolean isHumanNationalTeam(Object root, Object team) {
        return isHumanManagedTeam(team) && isNationalTeam(root, team);
    }

    private boolean isNationalTeam(Object root, Object team) {
        if (team == null) return false;
        for (Object nationalTeam : list(call(root, "aG"))) {
            if (sameReference(call(nationalTeam, "jn"), team)) return true;
        }
        return false;
    }

    private boolean isNationalTournament(String competition) {
        String normalized = Normalizer.normalize(competition, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        return normalized.contains("copa")
                || normalized.contains("eliminatoria")
                || normalized.contains("qualif")
                || normalized.contains("nations")
                || normalized.contains("euro")
                || normalized.contains("world cup")
                || normalized.contains("mundial")
                || normalized.contains("championship")
                || normalized.contains("campeonato")
                || normalized.contains(" cup")
                || normalized.startsWith("cup ");
    }

    private void setCalendarDayType(Object day, int dayType) {
        try {
            ReflectionUtils.setFieldValue(day, CALENDAR_DAY_TYPE_FIELD, dayType);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel marcar o dia como data de jogo no calendario.", e);
        }
    }

    private void moveHumanMatchesFirst(List<Object> matches) {
        List<Object> humanMatches = matches.stream().filter(this::hasHumanManagedTeam).toList();
        if (humanMatches.isEmpty()) return;
        matches.removeAll(humanMatches);
        matches.addAll(0, humanMatches);
    }

    private void validateRoundOrder(List<Object> days, String key, LocalDate from, LocalDate to) {
        LocalDate previous = null;
        LocalDate next = null;
        for (Object day : days) {
            LocalDate date = dayDate(day);
            if (date.equals(from)) continue;
            boolean hasCompetition = list(call(day, "h")).stream().anyMatch(match ->
                    competitionKey(match).equals(key) && isPointsStage(call(match, "ht")));
            if (!hasCompetition) continue;
            if (date.isBefore(from) && (previous == null || date.isAfter(previous))) previous = date;
            if (date.isAfter(from) && (next == null || date.isBefore(next))) next = date;
        }
        if (previous != null && !to.isAfter(previous)) {
            throw new IllegalArgumentException("Essa rodada precisa continuar depois da rodada de " + previous + ".");
        }
        if (next != null && !to.isBefore(next)) {
            throw new IllegalArgumentException("Essa rodada precisa continuar antes da proxima, em " + next + ". Mova primeiro a ultima rodada.");
        }
    }

    private void validateNoClubCollision(Object targetDay, List<Object> moving, String key) {
        IdentityHashMap<Object, Boolean> movingTeams = new IdentityHashMap<>();
        for (Object match : moving) {
            addTeam(movingTeams, call(match, "hc"));
            addTeam(movingTeams, call(match, "hd"));
        }
        for (Object existing : list(call(targetDay, "h"))) {
            if (competitionKey(existing).equals(key)) {
                throw new IllegalArgumentException("Ja existe uma rodada dessa competicao na data escolhida.");
            }
            Object home = call(existing, "hc");
            Object away = call(existing, "hd");
            if (movingTeams.containsKey(home)) {
                throw new IllegalArgumentException("Um dos clubes da rodada ja tem outro jogo na data escolhida: " + teamName(home) + ".");
            }
            if (movingTeams.containsKey(away)) {
                throw new IllegalArgumentException("Um dos clubes da rodada ja tem outro jogo na data escolhida: " + teamName(away) + ".");
            }
        }
    }

    private void pruneDayMetadata(Object day, List<Object> remainingMatches) {
        List<Object> stages = list(call(day, "j"));
        List<Object> competitions = list(call(day, "t"));
        stages.removeIf(stage -> remainingMatches.stream().noneMatch(match -> sameReference(call(match, "ht"), stage)));
        competitions.removeIf(competition -> remainingMatches.stream().noneMatch(match -> sameReference(call(match, "hy"), competition)));
    }

    private LocalDate currentDate(Object root) {
        Calendar calendar = (Calendar) call(root, "bb");
        return toLocalDate(calendar);
    }

    private List<Object> calendarDays(Object root) {
        try {
            return list(ReflectionUtils.getFieldValue(root, CALENDAR_FIELD));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Esse save nao contem o calendario editavel do Brasfoot.", e);
        }
    }

    private Object findDay(List<Object> days, LocalDate date) {
        return days.stream().filter(day -> dayDate(day).equals(date)).findFirst().orElse(null);
    }

    private LocalDate dayDate(Object day) {
        return toLocalDate((Calendar) call(day, "a"));
    }

    private LocalDate toLocalDate(Calendar calendar) {
        return calendar.toInstant().atZone(calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    private String competitionKey(Object match) {
        return competitionObjectKey(call(match, "hy"));
    }

    private String competitionObjectKey(Object competition) {
        return competition.getClass().getName() + "|" + call(competition, "b") + "|" + competitionName(competition)
                + "|" + System.identityHashCode(competition);
    }

    private String competitionName(Object competition) {
        String name = optionalString(competition, "getNome");
        if (name.isBlank()) name = optionalString(competition, "is");
        return name.isBlank() ? competition.getClass().getSimpleName() : name;
    }

    private String teamName(Object team) {
        try {
            Object name = ReflectionUtils.getFieldValue(team, "dm");
            return name == null ? team.getClass().getSimpleName() : String.valueOf(name);
        } catch (ReflectiveOperationException e) {
            return team.getClass().getSimpleName();
        }
    }

    private void addTeam(IdentityHashMap<Object, Boolean> teams, Object team) {
        if (team != null) teams.put(team, Boolean.TRUE);
    }

    private boolean sameReference(Object first, Object second) {
        return first == second;
    }

    private String optionalString(Object target, String method) {
        try {
            Object value = call(target, method);
            return value == null ? "" : String.valueOf(value).trim();
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private Object call(Object target, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel ler " + methodName + " em " + target.getClass().getName(), e);
        }
    }

    private void invokeCompatible(Object target, String methodName, Object argument) {
        try {
            for (Method method : target.getClass().getMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == 1
                        && method.getParameterTypes()[0].isAssignableFrom(argument.getClass())) {
                    method.invoke(target, argument);
                    return;
                }
            }
            throw new NoSuchMethodException(methodName + "(" + argument.getClass().getName() + ")");
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel atualizar as referencias da rodada no calendario.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> list(Object value) {
        if (!(value instanceof List<?>)) throw new IllegalStateException("Estrutura inesperada no calendario do save.");
        return (List<Object>) value;
    }

    private record GroupKey(String competitionKey, LocalDate date) { }

    private static final class RoundGroup {
        private final String competitionKey;
        private final LocalDate date;
        private final String competition;
        private int matchCount;

        private RoundGroup(String competitionKey, LocalDate date, String competition) {
            this.competitionKey = competitionKey;
            this.date = date;
            this.competition = competition;
        }

        private CalendarRoundRow toRow(String phase) {
            return new CalendarRoundRow(competitionKey, date, competition, phase, matchCount);
        }
    }
}
