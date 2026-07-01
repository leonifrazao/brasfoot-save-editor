package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.BrasfootGameLibraryPort;
import br.com.saveeditor.brasfoot.domain.League;
import br.com.saveeditor.brasfoot.domain.LeagueTableEntry;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.util.BrasfootConstants;
import br.com.saveeditor.brasfoot.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LeagueManagementService {

    private static final Logger log = LoggerFactory.getLogger(LeagueManagementService.class);

    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;
    private final BrasfootGameLibraryPort gameLibraryPort;

    public LeagueManagementService(SessionStatePort sessionStatePort, SessionResolver sessionResolver,
                                   BrasfootGameLibraryPort gameLibraryPort) {
        this.sessionStatePort = sessionStatePort;
        this.sessionResolver = sessionResolver;
        this.gameLibraryPort = gameLibraryPort;
    }

    public List<League> getLeagues(UUID sessionId) {
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        List<League> leagues = new ArrayList<>();

        collectLeagues(root, leagues, BrasfootConstants.ROOT_PRIMARY_COUNTRIES);
        collectLeagues(root, leagues, BrasfootConstants.ROOT_SECONDARY_COUNTRIES);

        return leagues;
    }

    public List<LeagueTableEntry> getTable(UUID sessionId, String leagueId) {
        Object root = sessionResolver.loadRequired(sessionId).getContext().getState().getObjetoRaiz();
        Object league = resolveLeague(root, leagueId);
        List<Object> teams = leagueTeams(league);
        List<LeagueTableEntry> table = new ArrayList<>();

        for (int i = 0; i < teams.size(); i++) {
            table.add(toEntry(i + 1, teams.get(i), league));
        }

        return table;
    }

    public LeagueTableEntry updateTableEntry(UUID sessionId, String leagueId, int teamId, int points, int wins,
                                             int draws, int losses, int goalsFor, int goalsAgainst) {
        Session session = sessionResolver.loadRequired(sessionId);
        Object root = session.getContext().getState().getObjetoRaiz();
        Object league = resolveLeague(root, leagueId);
        Object team = findLeagueTeam(league, teamId);
        int played = wins + draws + losses;

        try {
            Object stats = statsObject(team, league);
            if (stats == null) {
                stats = createStats(team, league);
            }
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_POINTS, points);
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_PLAYED, played);
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_WINS, wins);
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_LOSSES, losses);
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_GOALS_FOR, goalsFor);
            ReflectionUtils.setFieldValue(stats, BrasfootConstants.LEAGUE_STATS_GOALS_AGAINST, goalsAgainst);
            sessionStatePort.save(session);
            return toEntry(positionOf(league, teamId), team, league);
        } catch (ReflectiveOperationException e) {
            log.warn("Failed to update league table entry", e);
            throw new IllegalStateException("Could not update league table entry", e);
        }
    }

    private void collectLeagues(Object root, List<League> leagues, String countryListField) {
        Object countriesValue;
        try {
            countriesValue = ReflectionUtils.getFieldValue(root, countryListField);
        } catch (ReflectiveOperationException e) {
            return;
        }

        if (!(countriesValue instanceof List<?> countries)) {
            return;
        }

        for (int countryIndex = 0; countryIndex < countries.size(); countryIndex++) {
            Object country = countries.get(countryIndex);
            Object divisionsValue;
            try {
                divisionsValue = ReflectionUtils.getFieldValue(country, BrasfootConstants.COUNTRY_DIVISIONS);
            } catch (ReflectiveOperationException e) {
                continue;
            }
            if (!(divisionsValue instanceof List<?> divisions)) {
                continue;
            }
            for (int divisionIndex = 0; divisionIndex < divisions.size(); divisionIndex++) {
                Object division = divisions.get(divisionIndex);
                addLeague(leagues, division, path(countryListField, countryIndex, divisionIndex, BrasfootConstants.DIVISION_MAIN_LEAGUE), BrasfootConstants.DIVISION_MAIN_LEAGUE);
                addLeague(leagues, division, path(countryListField, countryIndex, divisionIndex, BrasfootConstants.DIVISION_SECONDARY_LEAGUE), BrasfootConstants.DIVISION_SECONDARY_LEAGUE);
            }
        }
    }

    private void addLeague(List<League> leagues, Object division, String path, String leagueField) {
        try {
            Object league = ReflectionUtils.getFieldValue(division, leagueField);
            if (league == null || !league.getClass().getName().equals("f.s")) {
                return;
            }
            leagues.add(new League(path, leagueLabel(league), path, leagueTeams(league).size()));
        } catch (ReflectiveOperationException ignored) {
            // Division has no league at this slot.
        }
    }

    private Object resolveLeague(Object root, String leagueId) {
        for (League league : getLeaguesFromRoot(root)) {
            if (league.id().equals(leagueId)) {
                return resolvePath(root, league.path());
            }
        }
        throw new IllegalArgumentException("League not found: " + leagueId);
    }

    private List<League> getLeaguesFromRoot(Object root) {
        List<League> leagues = new ArrayList<>();
        collectLeagues(root, leagues, BrasfootConstants.ROOT_PRIMARY_COUNTRIES);
        collectLeagues(root, leagues, BrasfootConstants.ROOT_SECONDARY_COUNTRIES);
        return leagues;
    }

    private Object resolvePath(Object root, String path) {
        Object current = root;
        String relativePath = path.startsWith("root.") ? path.substring(5) : path;
        for (String token : relativePath.split("\\.")) {
            current = resolveToken(current, token);
        }
        return current;
    }

    private Object resolveToken(Object current, String token) {
        try {
            int bracket = token.indexOf('[');
            if (bracket < 0) {
                return ReflectionUtils.getFieldValue(current, token);
            }
            String fieldName = token.substring(0, bracket);
            int index = Integer.parseInt(token.substring(bracket + 1, token.indexOf(']')));
            Object listValue = ReflectionUtils.getFieldValue(current, fieldName);
            if (listValue instanceof List<?> list) {
                return list.get(index);
            }
            throw new IllegalArgumentException("Path token is not a list: " + token);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Invalid league path token: " + token, e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> leagueTeams(Object league) {
        try {
            Object teams = ReflectionUtils.getFieldValue(league, BrasfootConstants.LEAGUE_TEAMS);
            if (teams instanceof List<?> list) {
                return (List<Object>) list;
            }
            return List.of();
        } catch (ReflectiveOperationException e) {
            return List.of();
        }
    }

    private Object findLeagueTeam(Object league, int teamId) {
        return leagueTeams(league).stream()
                .filter(team -> teamId(team) == teamId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team not found in league: " + teamId));
    }

    private int positionOf(Object league, int teamId) {
        List<Object> teams = leagueTeams(league);
        for (int i = 0; i < teams.size(); i++) {
            if (teamId(teams.get(i)) == teamId) {
                return i + 1;
            }
        }
        return 0;
    }

    private LeagueTableEntry toEntry(int position, Object team, Object league) {
        int[] stats = statsArray(team, league);
        return new LeagueTableEntry(position, teamId(team), teamName(team), stats[0], stats[1], stats[2], stats[3],
                stats[4], stats[5], stats[6], stats[7]);
    }

    private int[] statsArray(Object team, Object league) {
        try {
            Method d = team.getClass().getDeclaredMethod("d", league.getClass());
            d.setAccessible(true);
            Object raw = d.invoke(team, league);
            if (raw != null && raw.getClass().isArray() && Array.getLength(raw) >= 8) {
                int[] out = new int[8];
                for (int i = 0; i < out.length; i++) {
                    out[i] = ((Number) Array.get(raw, i)).intValue();
                }
                return out;
            }
        } catch (ReflectiveOperationException ignored) {
            // Fall back to best.ak fields.
        }

        try {
            Object stats = statsObject(team, league);
            int points = intField(stats, BrasfootConstants.LEAGUE_STATS_POINTS);
            int played = intField(stats, BrasfootConstants.LEAGUE_STATS_PLAYED);
            int wins = intField(stats, BrasfootConstants.LEAGUE_STATS_WINS);
            int losses = intField(stats, BrasfootConstants.LEAGUE_STATS_LOSSES);
            int goalsFor = intField(stats, BrasfootConstants.LEAGUE_STATS_GOALS_FOR);
            int goalsAgainst = intField(stats, BrasfootConstants.LEAGUE_STATS_GOALS_AGAINST);
            int draws = played - wins - losses;
            int goalDifference = goalsFor - goalsAgainst;
            return new int[]{points, played, wins, draws, losses, goalsFor, goalsAgainst, goalDifference};
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not read league stats", e);
        }
    }

    private Object statsObject(Object team, Object league) throws ReflectiveOperationException {
        Method c = team.getClass().getDeclaredMethod("c", league.getClass());
        c.setAccessible(true);
        return c.invoke(team, league);
    }

    private Object createStats(Object team, Object league) throws ReflectiveOperationException {
        Class<?> akClass = Class.forName("best.ak", true, gameLibraryPort.getClassLoader());
        Constructor<?> ctor = akClass.getDeclaredConstructor(team.getClass(), league.getClass());
        ctor.setAccessible(true);
        Object stats = ctor.newInstance(team, league);

        Field noField = team.getClass().getDeclaredField("no");
        noField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Object> statsList = (List<Object>) noField.get(team);
        statsList.add(stats);

        return stats;
    }

    private int teamId(Object team) {
        return intFieldUnchecked(team, BrasfootConstants.TEAM_ID);
    }

    private String teamName(Object team) {
        try {
            Object value = ReflectionUtils.getFieldValue(team, BrasfootConstants.TEAM_NAME);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private int intField(Object obj, String fieldName) throws ReflectiveOperationException {
        return ((Number) ReflectionUtils.getFieldValue(obj, fieldName)).intValue();
    }

    private int intFieldUnchecked(Object obj, String fieldName) {
        try {
            return intField(obj, fieldName);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not read field: " + fieldName, e);
        }
    }

    private String leagueLabel(Object league) {
        String leagueName = stringField(league, BrasfootConstants.LEAGUE_NAME);
        String divisionName = stringField(league, BrasfootConstants.LEAGUE_DIVISION_NAME);
        String fallbackName = stringField(league, BrasfootConstants.LEAGUE_FALLBACK_NAME);
        String label = (leagueName + " - " + divisionName).trim();
        if (label.equals("-") || label.isBlank()) {
            label = fallbackName;
        }
        return label.replaceAll("\\s+", " ").trim();
    }

    private String stringField(Object obj, String fieldName) {
        try {
            Object value = ReflectionUtils.getFieldValue(obj, fieldName);
            return value == null ? "" : String.valueOf(value);
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private String path(String countryListField, int countryIndex, int divisionIndex, String leagueField) {
        return String.format("root.%s[%d].%s[%d].%s", countryListField, countryIndex,
                BrasfootConstants.COUNTRY_DIVISIONS, divisionIndex, leagueField);
    }
}
