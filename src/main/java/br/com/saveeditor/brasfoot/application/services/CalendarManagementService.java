package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.enums.Country;
import br.com.saveeditor.brasfoot.presentation.model.CalendarRoundRow;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class CalendarManagementService {
    private static final String CALENDAR_FIELD = "as";
    private static final String MATCH_DAY_INDEX_FIELD = "fy";
    private static final String CALENDAR_DAY_TYPE_FIELD = "w";
    private static final int HUMAN_MATCH_DAY_TYPE = 200;
    private static final String TEAM_IS_HUMAN_FIELD = "mW";
    private static final String POINTS_STAGE_CLASS = "f.s";
    private static final String NATIONAL_CUP_CLASS = "f.f";
    private static final String KNOCKOUT_STAGE_CLASS = "f.z";
    private static final String CONTINENTAL_PRELIMINARY_STAGE_CLASS = "f.ContinentalPreliminaryStage";
    private static final String KNOCKOUT_ROUND_DAY_INDICES = "as";
    private static final String KNOCKOUT_ROUND_CATEGORY = "w";
    private static final int REAL_INTERCONTINENTAL_PHASE = 150;
    private static final int NATIONAL_CUP_KIND = 2;
    private static final Set<Integer> CUP_CATEGORIES = Set.of(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14);
    private static final Set<Integer> NATIONAL_TEAM_TOURNAMENTS = Set.of(7, 9, 13, 14);

    private final SessionResolver sessionResolver;
    private final SessionStatePort sessionStatePort;

    public CalendarManagementService(SessionResolver sessionResolver, SessionStatePort sessionStatePort) {
        this.sessionResolver = sessionResolver;
        this.sessionStatePort = sessionStatePort;
    }

    public List<CalendarRoundRow> getUpcomingRounds(UUID sessionId) {
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        LocalDate currentDate = currentDate(root);
        Map<GroupKey, RoundGroup> rounds = collectRounds(calendarDays(root));
        Map<String, List<RoundGroup>> byCompetition = new LinkedHashMap<>();
        rounds.values().stream().sorted(Comparator.comparing((RoundGroup round) -> round.date)
                        .thenComparing(round -> round.competition))
                .forEach(round -> byCompetition.computeIfAbsent(round.competitionKey, ignored -> new ArrayList<>()).add(round));

        List<CalendarRoundRow> result = new ArrayList<>();
        for (List<RoundGroup> competitionRounds : byCompetition.values()) {
            for (int index = 0; index < competitionRounds.size(); index++) {
                RoundGroup round = competitionRounds.get(index);
                if (round.date.isAfter(currentDate)) {
                    result.add(round.toRow(index + 1));
                }
            }
        }
        return result.stream().sorted(Comparator.comparing(CalendarRoundRow::date)
                .thenComparing(CalendarRoundRow::competition)).toList();
    }

    public int moveRound(UUID sessionId, String eventKey, LocalDate sourceDate, LocalDate targetDate) {
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

        var rounds = collectRounds(days);
        RoundGroup round = findRound(rounds, eventKey, sourceDate);
        if (round == null) {
            throw new IllegalArgumentException("A rodada selecionada nao esta mais nessa data. Atualize o calendario e tente novamente.");
        }

        int targetIndex = identityIndex(days, targetDay);
        validateRoundOrder(rounds, round, targetDate);
        validateNoClubCollision(targetDay, round.matches, round.competitionObject);
        if (round.knockoutLeg != null) validateKnockoutMove(days, rounds, round, targetIndex, currentDate);

        List<Object> sourceMatches = list(call(sourceDay, "h"));
        List<Object> destinationMatches = list(call(targetDay, "h"));
        sourceMatches.removeIf(round.seen::contains);
        for (Object match : round.matches) {
            updateMatchDayIndex(match, targetIndex);
            destinationMatches.add(match);
            invokeCompatible(targetDay, "a", call(match, "ht"));
            invokeCompatible(targetDay, "a", call(match, "hy"));
        }

        pruneDayMetadata(sourceDay, sourceMatches, round.matches);
        moveHumanMatchesFirst(destinationMatches);
        if (round.knockoutLeg == null) {
            updateCalendarDayType(sourceDay, sourceMatches);
            updateCalendarDayType(targetDay, destinationMatches);
        } else {
            round.knockoutLeg.dayIndices.set(round.knockoutLeg.leg, targetIndex);
            // Draws use the existing reserved categories, and the game skips untyped days.
            if (dayType(targetDay) <= 0) setCalendarDayType(targetDay, round.knockoutLeg.category);
        }
        sessionStatePort.save(session);
        return round.matches.size();
    }

    public boolean hasHumanNationalTournamentConflict(UUID sessionId, String eventKey,
                                                      LocalDate sourceDate, LocalDate targetDate) {
        Objects.requireNonNull(sourceDate, "Data atual da rodada obrigatoria");
        Objects.requireNonNull(targetDate, "Nova data obrigatoria");
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        List<Object> days = calendarDays(root);
        Object sourceDay = findDay(days, sourceDate);
        Object targetDay = findDay(days, targetDate);
        if (sourceDay == null || targetDay == null) return false;

        int sourceIndex = identityIndex(days, sourceDay);
        RoundGroup round = findRound(collectRounds(days, sourceIndex, sourceIndex + 1), eventKey, sourceDate);
        if (round == null) return false;
        List<Object> destination = list(call(targetDay, "h"));
        return (round.matches.stream().anyMatch(match -> isHumanClubMatch(root, match))
                && destination.stream().anyMatch(match -> isHumanNationalTournamentMatch(root, match)))
                || (round.matches.stream().anyMatch(match -> isHumanNationalTournamentMatch(root, match))
                && destination.stream().anyMatch(match -> isHumanClubMatch(root, match)));
    }

    private Map<GroupKey, RoundGroup> collectRounds(List<Object> days) {
        return collectRounds(days, 0, days.size());
    }

    private Map<GroupKey, RoundGroup> collectRounds(List<Object> days, int start, int end) {
        Map<GroupKey, RoundGroup> groups = new LinkedHashMap<>();
        Map<Object, Map<Object, KnockoutLeg>> knockoutStages = new IdentityHashMap<>();
        Map<Object, String> competitionKeys = new IdentityHashMap<>();
        for (int dayIndex = start; dayIndex < end; dayIndex++) {
            Object day = days.get(dayIndex);
            LocalDate date = dayDate(day);
            for (Object match : list(call(day, "h"))) {
                Object competition = call(match, "hy");
                Object stage = call(match, "ht");
                if (competition == null || stage == null) continue;
                KnockoutLeg knockoutLeg = null;
                if (isKnockoutStage(stage) && CUP_CATEGORIES.contains(((Number) call(competition, "b")).intValue())) {
                    knockoutLeg = knockoutStages.computeIfAbsent(stage, this::indexKnockoutStage).get(match);
                    if (knockoutLeg == null || knockoutLeg.dayIndex() != dayIndex) continue;
                } else if (!POINTS_STAGE_CLASS.equals(stage.getClass().getName())) {
                    continue;
                }
                String competitionKey = competitionKeys.computeIfAbsent(competition, this::competitionObjectKey);
                String eventKey = knockoutLeg == null ? competitionKey : competitionKey + "|" + knockoutLeg.key();
                GroupKey groupKey = new GroupKey(eventKey, date);
                KnockoutLeg leg = knockoutLeg;
                RoundGroup group = groups.computeIfAbsent(groupKey,
                        ignored -> new RoundGroup(competitionKey, eventKey, date, competitionName(competition),
                                competition, nationalCupCountry(competition), leg));
                if (group.seen.add(match)) group.matches.add(match);
            }
        }
        return groups;
    }

    private RoundGroup findRound(Map<GroupKey, RoundGroup> groups, String key, LocalDate date) {
        RoundGroup selected = groups.get(new GroupKey(key, date));
        if (selected != null) return selected;
        // Accept a competition key from a caller when it identifies exactly one event on the date.
        List<RoundGroup> sameDay = groups.values().stream()
                .filter(group -> group.date.equals(date) && group.competitionKey.equals(key)).toList();
        return sameDay.size() == 1 ? sameDay.get(0) : null;
    }

    private boolean isKnockoutStage(Object stage) {
        String type = stage.getClass().getName();
        return KNOCKOUT_STAGE_CLASS.equals(type) || CONTINENTAL_PRELIMINARY_STAGE_CLASS.equals(type);
    }

    private Map<Object, KnockoutLeg> indexKnockoutStage(Object stage) {
        Map<Object, KnockoutLeg> byMatch = new IdentityHashMap<>();
        int stageCode = ((Number) call(stage, "zf")).intValue();
        // The Real Brasfoot may rebuild this competition on load and on match day.
        if (stageCode == REAL_INTERCONTINENTAL_PHASE) return byMatch;

        for (Object round : list(call(stage, "zp"))) {
            try {
                List<Object> days = list(ReflectionUtils.getFieldValue(round, KNOCKOUT_ROUND_DAY_INDICES));
                int category = ((Number) ReflectionUtils.getFieldValue(round, KNOCKOUT_ROUND_CATEGORY)).intValue();
                if (!CUP_CATEGORIES.contains(category)) continue;
                int format = ((Number) call(round, "ze")).intValue();
                int participants = ((Number) call(round, "zU")).intValue();
                int number = ((Number) call(round, "lU")).intValue() + 1;
                String namedStage = namedKnockoutStage(stage, stageCode);
                String phase = namedStage == null ? knockoutPhase(format, participants, number) : namedStage;
                boolean finalPhase = participants == 2 && namedStage == null && format != 2129 && format != 2229
                        && format != 2329;
                Set<Integer> drawCategories = successorCategories(category, stageCode);
                indexKnockoutLeg(byMatch, round, days, 0, list(call(round, "zW")), phase, category,
                        drawCategories, finalPhase);
                indexKnockoutLeg(byMatch, round, days, 1, list(call(round, "zX")), phase, category,
                        drawCategories, finalPhase);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Não foi possível ler as datas do mata-mata no save.", e);
            }
        }
        return byMatch;
    }

    private String namedKnockoutStage(Object stage, int code) {
        if (CONTINENTAL_PRELIMINARY_STAGE_CLASS.equals(stage.getClass().getName())) {
            String[] names = (String[]) call(stage, "zB");
            return names.length == 0 ? "Fase preliminar" : names[0];
        }
        return switch (code) {
            case 4000 -> "Pré-eliminatória";
            case 4001, 4002, 4003 -> "Eliminatória " + (code - 4000);
            case 4101, 4102, 4103 -> "Fase preliminar " + (code - 4100);
            case 4004 -> "Play-off de acesso";
            case 4006, 6006, 12006 -> "Play-off do mata-mata";
            case 6001 -> "Eliminatória 1";
            case 6002, 6004 -> "Eliminatória 2";
            case 12001, 12002, 12003, 12004 -> "Eliminatória " + (code - 12000);
            case 6100 -> "Fase preliminar";
            case 14006 -> "Play-off de rebaixamento";
            default -> null;
        };
    }

    private Set<Integer> successorCategories(int category, int stageCode) {
        int dependentCategory = switch (stageCode) {
            case 4000, 4001 -> 12; // Champions League to Conference League.
            case 4002, 4003, 4004, 4103 -> 6; // Europa League or Sudamericana.
            default -> category;
        };
        return category == dependentCategory ? Set.of(category) : Set.of(category, dependentCategory);
    }

    private String knockoutPhase(int format, int participants, int number) {
        return switch (format) {
            case 2129 -> "Primeira fase";
            case 2229 -> "Segunda fase";
            case 2329 -> "Terceira fase";
            default -> switch (participants) {
                case 2 -> "Final";
                case 4 -> "Semifinal";
                case 8 -> "Quartas de final";
                case 16 -> "Oitavas de final";
                case 32 -> "Fase de 32 clubes";
                default -> "Fase " + number;
            };
        };
    }

    private void indexKnockoutLeg(Map<Object, KnockoutLeg> byMatch, Object round, List<Object> days, int leg,
                                  List<Object> fixtures, String phase, int category, Set<Integer> drawCategories,
                                  boolean finalPhase) {
        if (fixtures.isEmpty() || leg >= days.size() || !(days.get(leg) instanceof Integer index) || index < 0) return;
        if (days.size() == 2) phase += leg == 0 ? " (ida)" : " (volta)";
        String key = System.identityHashCode(round) + "|" + leg;
        KnockoutLeg knockoutLeg = new KnockoutLeg(key, days, leg, index, fixtures, phase, category,
                drawCategories, finalPhase);
        for (Object match : fixtures) byMatch.put(match, knockoutLeg);
    }

    private String nationalCupCountry(Object competition) {
        if (!NATIONAL_CUP_CLASS.equals(competition.getClass().getName())
                || ((Number) call(competition, "b")).intValue() != NATIONAL_CUP_KIND) return null;
        Object country = call(competition, "yg");
        if (country == null) return null;
        int id = ((Number) call(country, "jc")).intValue();
        Country mapped = Country.fromId(id);
        if (mapped != null) return mapped.getName();
        String name = optionalString(country, "jf");
        return name.isBlank() ? null : name;
    }

    private void validateKnockoutMove(List<Object> days, Map<GroupKey, RoundGroup> groups,
                                      RoundGroup round, int targetIndex, LocalDate currentDate) {
        KnockoutLeg leg = round.knockoutLeg;
        if (dayFinished(days.get(leg.dayIndex)) || dayFinished(days.get(targetIndex))
                || !leg.fixtures.stream().allMatch(round.seen::contains) || leg.dayIndices.size() > 2
                || round.matches.stream().anyMatch(match -> Boolean.TRUE.equals(call(match, "e"))
                        || ((Number) call(match, "hM")).intValue() != leg.dayIndex)) {
            throw new IllegalArgumentException("As referências da fase estão incompletas no calendário do save ou já foram jogadas.");
        }
        if (leg.dayIndices.size() == 2) {
            Object other = leg.dayIndices.get(1 - leg.leg);
            if (!(other instanceof Integer otherIndex) || otherIndex < 0 || otherIndex >= days.size()
                    || (leg.leg == 0 ? targetIndex >= otherIndex : targetIndex <= otherIndex)) {
                throw new IllegalArgumentException("A ida precisa ficar antes da volta.");
            }
        }
        for (int i = 0; i < days.size(); i++) {
            if (i != leg.dayIndex && list(call(days.get(i), "h")).stream().anyMatch(round.seen::contains)) {
                throw new IllegalArgumentException("Há partidas desta fase em mais de um dia no save.");
            }
        }
        if (dayType(days.get(targetIndex)) <= 0 && leg.category != NATIONAL_CUP_KIND) {
            validateNewKnockoutDate(groups, round, dayDate(days.get(targetIndex)), currentDate);
        }

        if (targetIndex <= leg.dayIndex || !leg.canAdvance()) return;
        for (int i = leg.dayIndex + 1; i <= targetIndex; i++) {
            Object day = days.get(i);
            if (leg.drawCategories.contains(dayType(day)) && !dayFinished(day)) {
                throw new IllegalArgumentException("Escolha uma data anterior à próxima data reservada para sorteios do mata-mata ("
                        + dayDate(day) + ").");
            }
        }
    }

    private void validateNewKnockoutDate(Map<GroupKey, RoundGroup> groups, RoundGroup moving,
                                         LocalDate target, LocalDate currentDate) {
        boolean competingDraw = groups.values().stream().anyMatch(other ->
                other.competitionObject != moving.competitionObject && other.knockoutLeg != null
                        && other.knockoutLeg.canAdvance()
                        && other.knockoutLeg.drawCategories.contains(moving.knockoutLeg.category)
                        && other.date.isAfter(currentDate) && other.date.isBefore(target));
        if (competingDraw) {
            throw new IllegalArgumentException("Essa data livre pode ser usada por um sorteio de outra competição. "
                    + "Escolha um dia já previsto no calendário ou uma data anterior ao sorteio.");
        }
    }

    private int dayType(Object day) {
        return ((Number) call(day, "b")).intValue();
    }

    private boolean dayFinished(Object day) {
        return Boolean.TRUE.equals(call(day, "e"));
    }

    private int identityIndex(List<Object> items, Object target) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == target) return i;
        }
        throw new IllegalStateException("Dia não encontrado no calendário.");
    }

    private void updateMatchDayIndex(Object match, int dayIndex) {
        try {
            ReflectionUtils.setFieldValue(match, MATCH_DAY_INDEX_FIELD, dayIndex);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel atualizar a data gravada na partida do Brasfoot.", e);
        }
    }

    private void updateCalendarDayType(Object day, List<Object> matches) {
        int currentType = dayType(day);
        if (CUP_CATEGORIES.contains(currentType)) return;
        if (matches.isEmpty()) {
            setCalendarDayType(day, 0);
            return;
        }

        boolean hasHumanManagedMatch = matches.stream().anyMatch(this::hasHumanManagedTeam);
        if (hasHumanManagedMatch) {
            setCalendarDayType(day, HUMAN_MATCH_DAY_TYPE);
            return;
        }

        if (currentType <= 0) {
            setCalendarDayType(day, ((Number) call(call(matches.get(0), "hy"), "b")).intValue());
        }
    }

    private boolean hasHumanManagedTeam(Object match) {
        return isHumanManagedTeam(call(match, "hc")) || isHumanManagedTeam(call(match, "hd"));
    }

    private boolean isHumanManagedTeam(Object team) {
        if (team == null) return false;
        try {
            return Boolean.TRUE.equals(ReflectionUtils.getFieldValue(team, TEAM_IS_HUMAN_FIELD));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Nao foi possivel identificar o time controlado pelo tecnico humano.", e);
        }
    }

    private boolean isHumanClub(Object root, Object team) {
        return isHumanManagedTeam(team) && !isNationalTeam(root, team);
    }

    private boolean isHumanClubMatch(Object root, Object match) {
        return isHumanClub(root, call(match, "hc")) || isHumanClub(root, call(match, "hd"));
    }

    private boolean isHumanNationalTournamentMatch(Object root, Object match) {
        Object competition = call(match, "hy");
        return competition != null && NATIONAL_TEAM_TOURNAMENTS.contains(((Number) call(competition, "b")).intValue())
                && (isHumanNationalTeam(root, call(match, "hc")) || isHumanNationalTeam(root, call(match, "hd")));
    }

    private boolean isHumanNationalTeam(Object root, Object team) {
        return isHumanManagedTeam(team) && isNationalTeam(root, team);
    }

    private boolean isNationalTeam(Object root, Object team) {
        if (team == null) return false;
        for (Object nationalTeam : list(call(root, "aG"))) {
            if (call(nationalTeam, "jn") == team) return true;
        }
        return false;
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

    private void validateRoundOrder(Map<GroupKey, RoundGroup> groups, RoundGroup moving, LocalDate to) {
        LocalDate previous = null;
        LocalDate next = null;
        for (RoundGroup group : groups.values()) {
            LocalDate date = group.date;
            if (group.competitionObject != moving.competitionObject || date.equals(moving.date)) continue;
            if (date.isBefore(moving.date) && (previous == null || date.isAfter(previous))) previous = date;
            if (date.isAfter(moving.date) && (next == null || date.isBefore(next))) next = date;
        }
        if (previous != null && !to.isAfter(previous)) {
            throw new IllegalArgumentException("Essa rodada precisa continuar depois da rodada de " + previous + ".");
        }
        if (next != null && !to.isBefore(next)) {
            throw new IllegalArgumentException("Essa rodada precisa continuar antes da proxima, em " + next + ". Mova primeiro a ultima rodada.");
        }
    }

    private void validateNoClubCollision(Object targetDay, List<Object> moving, Object competition) {
        IdentityHashMap<Object, Boolean> movingTeams = new IdentityHashMap<>();
        for (Object match : moving) {
            addTeam(movingTeams, call(match, "hc"));
            addTeam(movingTeams, call(match, "hd"));
        }
        for (Object existing : list(call(targetDay, "h"))) {
            if (call(existing, "hy") == competition) {
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

    private void pruneDayMetadata(Object day, List<Object> remainingMatches, List<Object> movedMatches) {
        List<Object> stages = list(call(day, "j"));
        List<Object> competitions = list(call(day, "t"));
        removeMovedReferences(stages, remainingMatches, movedMatches, "ht");
        removeMovedReferences(competitions, remainingMatches, movedMatches, "hy");
    }

    private void removeMovedReferences(List<Object> references, List<Object> remaining, List<Object> moved,
                                       String getter) {
        Set<Object> removable = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object match : moved) removable.add(call(match, getter));
        for (Object match : remaining) removable.remove(call(match, getter));
        references.removeIf(removable::contains);
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

    private record GroupKey(String eventKey, LocalDate date) { }

    private record KnockoutLeg(String key, List<Object> dayIndices, int leg, int dayIndex, List<Object> fixtures,
                                String phase, int category, Set<Integer> drawCategories, boolean finalPhase) {
        private boolean canAdvance() {
            return !finalPhase && leg == dayIndices.size() - 1;
        }
    }

    private static final class RoundGroup {
        private final String competitionKey;
        private final String eventKey;
        private final LocalDate date;
        private final String competition;
        private final Object competitionObject;
        private final String country;
        private final KnockoutLeg knockoutLeg;
        private final List<Object> matches = new ArrayList<>();
        private final Set<Object> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        private RoundGroup(String competitionKey, String eventKey, LocalDate date, String competition,
                           Object competitionObject, String country, KnockoutLeg knockoutLeg) {
            this.competitionKey = competitionKey;
            this.eventKey = eventKey;
            this.date = date;
            this.competition = competition;
            this.competitionObject = competitionObject;
            this.country = country;
            this.knockoutLeg = knockoutLeg;
        }

        private CalendarRoundRow toRow(int index) {
            String phase = knockoutLeg == null ? "Rodada " + index : knockoutLeg.phase;
            return new CalendarRoundRow(competitionKey, date, competition, phase, matches.size(), country, eventKey);
        }
    }
}
