package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.enums.Country;
import br.com.saveeditor.brasfoot.presentation.model.CalendarRoundRow;
import f.f;
import f.s;
import f.z;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CalendarManagementServiceTest {
    private final UUID id = UUID.randomUUID();
    private final Root root = new Root();
    private final SessionStatePort state = mock(SessionStatePort.class);
    private final SessionResolver resolver = mock(SessionResolver.class);
    private final CalendarManagementService service = new CalendarManagementService(resolver, state);
    private Session session;

    @BeforeEach
    void openSession() {
        var context = new SaveContext();
        context.load(new NavegacaoState(root, null, "calendario.s22"), "test");
        session = new Session(id, context);
        when(resolver.loadRequired(id)).thenReturn(session);
    }

    @Test
    void listsDomesticCupsByCountryAndLegAlongsidePointsRounds() {
        var brazil = new f("Copa", Country.BRASIL.getId());
        var portugal = new f("Copa", Country.PORTUGAL.getId());
        var brazilianRound = new z.Round(4, 8, 2529, 2, 5);
        var portugueseRound = new z.Round(1, 4, -1, 2);
        var ida = cupMatch(brazil, install(brazilianRound), brazilianRound, 0);
        root.as.get(2).matches.add(ida);
        cupMatch(brazil, (z) ida.stage, brazilianRound, 1);
        cupMatch(portugal, install(portugueseRound), portugueseRound, 0);

        var league = new Competition("Liga", 1);
        var stage = new s();
        add(new Match(league, stage, 0));
        add(new Match(league, stage, 4));
        add(new Match(new Competition("Continental", 4), ida.stage, 4));

        var rows = service.getUpcomingRounds(id);
        assertEquals(4, rows.size());
        var brazilianRows = rows.stream().filter(row -> "Brasil".equals(row.country())).toList();
        assertEquals(List.of("Quartas de final (ida)", "Quartas de final (volta)"),
                brazilianRows.stream().map(CalendarRoundRow::phase).toList());
        assertEquals(1, brazilianRows.get(0).matchCount());
        assertEquals(brazilianRows.get(0).competitionKey(), brazilianRows.get(1).competitionKey());
        assertNotEquals(brazilianRows.get(0).eventKey(), brazilianRows.get(1).eventKey());
        assertEquals("Semifinal", rows.stream().filter(row -> "Portugal".equals(row.country()))
                .findFirst().orElseThrow().phase());
        assertEquals("Rodada 2", rows.stream().filter(row -> "Liga".equals(row.competition()))
                .findFirst().orElseThrow().phase());
    }

    @Test
    void movesOnlyTheRequestedLegAndSynchronizesBothDateIndicesAndDayMetadata() {
        var cup = new f("Copa", Country.BRASIL.getId());
        var round = new z.Round(4, 8, 2529, 3, 6);
        var stage = install(round);
        Match ida = cupMatch(cup, stage, round, 0);
        Match volta = cupMatch(cup, stage, round, 1);
        root.as.get(3).w = 2;
        root.as.get(3).matches.add(ida);
        var independent = new Competition("Independente", 4);
        root.as.get(3).a(independent);
        root.as.get(2).w = 4;

        var row = event("Quartas de final (ida)");
        assertEquals(1, service.moveRound(id, row.eventKey(), row.date(), date(2)));

        assertEquals(List.of(2, 6), round.as);
        assertEquals(2, ida.hM());
        assertEquals(6, volta.hM());
        assertEquals(List.of(ida), root.as.get(2).h());
        assertEquals(List.of(stage), root.as.get(2).j());
        assertEquals(List.of(cup), root.as.get(2).t());
        assertTrue(root.as.get(3).h().isEmpty());
        assertTrue(root.as.get(3).j().isEmpty());
        assertEquals(List.of(independent), root.as.get(3).t());
        assertEquals(2, root.as.get(3).b());
        assertEquals(4, root.as.get(2).b());
        assertEquals(date(2), event("Quartas de final (ida)").date());
        verify(state).save(session);
    }

    @Test
    void blocksReversedLegsAndProtectsTheNextReservedCupDate() {
        var cup = new f("Copa", Country.BRASIL.getId());
        var round = new z.Round(4, 8, 2529, 2, 4);
        var stage = install(round);
        cupMatch(cup, stage, round, 0);
        cupMatch(cup, stage, round, 1);
        root.as.get(6).w = 2;
        var ida = event("Quartas de final (ida)");
        var volta = event("Quartas de final (volta)");

        assertThrows(IllegalArgumentException.class, () -> service.moveRound(id, ida.eventKey(), date(2), date(5)));
        var reserved = assertThrows(IllegalArgumentException.class,
                () -> service.moveRound(id, volta.eventKey(), date(4), date(6)));
        assertTrue(reserved.getMessage().contains("reservada"));
        assertEquals(List.of(2, 4), round.as);
        verify(state, never()).save(any());
    }

    @Test
    void movesPointsRoundsWithTheExistingFlowAndRejectsClubCollisionsForCups() {
        var league = new Competition("Liga", 1);
        var leagueMatch = add(new Match(league, new s(), 3));
        var cup = new f("Copa", Country.PORTUGAL.getId());
        var round = new z.Round(1, 2, -1, 2);
        var finalMatch = cupMatch(cup, install(round), round, 0);
        finalMatch.home = leagueMatch.home;
        root.as.get(3).w = 1;
        var cupEvent = event("Final");
        assertThrows(IllegalArgumentException.class,
                () -> service.moveRound(id, cupEvent.eventKey(), cupEvent.date(), date(3)));
        verify(state, never()).save(any());

        var leagueEvent = event("Rodada 1");
        assertEquals(1, service.moveRound(id, leagueEvent.competitionKey(), date(3), date(4)));
        assertEquals(4, leagueMatch.hM());
        assertEquals(0, root.as.get(3).b());
        assertEquals(1, root.as.get(4).b());
        assertEquals(1, service.moveRound(id, cupEvent.eventKey(), date(2), date(3)));
        assertEquals(2, root.as.get(3).b());
    }

    @Test
    void detectsHumanClubAndNationalTeamConflictsForCupEvents() {
        var cup = new f("Copa", Country.BRASIL.getId());
        var round = new z.Round(1, 2, -1, 2);
        var cupMatch = cupMatch(cup, install(round), round, 0);
        cupMatch.home.mW = true;
        var internationalRound = new z.Round(0, 4, -1, 3);
        internationalRound.w = 9;
        var international = cupMatch(new Competition("Competição de seleções", 9),
                install(internationalRound), internationalRound, 0);
        international.home.mW = true;
        root.nationalTeams.add(new NationalTeam(international.home));

        var row = event("Final");
        assertTrue(service.hasHumanNationalTournamentConflict(id, row.eventKey(), date(2), date(3)));
        assertFalse(service.hasHumanNationalTournamentConflict(id, row.eventKey(), date(2), date(4)));
        var selection = event("Semifinal");
        assertTrue(service.hasHumanNationalTournamentConflict(id, selection.eventKey(), date(3), date(2)));
    }

    @Test
    void movesOnlyOneCompetitionWhenContinentalCupsShareADate() {
        var libertadores = new Competition("Libertadores", 4);
        var champions = new Competition("Champions League", 4);
        var libertadoresRound = new z.Round(2, 4, -1, 2, 5);
        var championsRound = new z.Round(0, 16, -1, 2, 5);
        libertadoresRound.w = championsRound.w = 4;

        Match libertadoresMatch = cupMatch(libertadores, install(libertadoresRound), libertadoresRound, 0);
        var championsStage = install(championsRound);
        Match championsMatch = cupMatch(champions, championsStage, championsRound, 0);
        root.as.get(2).w = 4;
        root.as.get(3).w = 6;
        var row = event("Semifinal (ida)");

        assertEquals(1, service.moveRound(id, row.eventKey(), date(2), date(3)));

        assertEquals(3, libertadoresMatch.hM());
        assertEquals(2, championsMatch.hM());
        assertEquals(List.of(3, 5), libertadoresRound.as);
        assertEquals(List.of(2, 5), championsRound.as);
        assertEquals(List.of(champions), root.as.get(2).t());
        assertEquals(List.of(championsStage), root.as.get(2).j());
        assertEquals(List.of(libertadores), root.as.get(3).t());
        assertEquals(4, root.as.get(2).b());
        assertEquals(6, root.as.get(3).b());
        assertEquals(date(2), event("Oitavas de final (ida)").date());
    }

    @Test
    void respectsTheDestinationCompetitionOfQualifyingLosers() {
        var champions = new Competition("Champions League", 4);
        var playoff = new z.Round(0, 12, -1, 1, 3);
        playoff.w = 4;
        var stage = install(playoff);
        stage.phaseCode = 4004;
        cupMatch(champions, stage, playoff, 0);
        cupMatch(champions, stage, playoff, 1);
        root.as.get(4).w = 6;

        var row = event("Play-off de acesso (volta)");
        var failure = assertThrows(IllegalArgumentException.class,
                () -> service.moveRound(id, row.eventKey(), date(3), date(5)));
        assertTrue(failure.getMessage().contains("reservada"));
        verify(state, never()).save(any());
    }

    @Test
    void ignoresLeaguePlayoffsAndStagesRebuiltByTheGame() {
        var promotion = new z.Round(0, 8, -1, 2);
        promotion.w = 1;
        cupMatch(new Competition("Liga", 1), install(promotion), promotion, 0);
        var fixed = new z.Round(0, 2, -1, 3);
        fixed.w = 5;
        var stage = install(fixed);
        stage.phaseCode = 150;
        cupMatch(new Competition("Intercontinental", 5), stage, fixed, 0);
        assertTrue(service.getUpcomingRounds(id).isEmpty());
    }

    @Test
    void avoidsCreatingAReservationThatAnotherCupCouldUseFirst() {
        var otherRound = new z.Round(0, 4, -1, 2);
        var finalRound = new z.Round(0, 2, -1, 4);
        otherRound.w = finalRound.w = 4;
        cupMatch(new Competition("Copa A", 4), install(otherRound), otherRound, 0);
        cupMatch(new Competition("Copa B", 4), install(finalRound), finalRound, 0);
        var row = event("Final");

        var failure = assertThrows(IllegalArgumentException.class,
                () -> service.moveRound(id, row.eventKey(), date(4), date(3)));
        assertTrue(failure.getMessage().contains("sorteio de outra competição"));
        assertEquals(1, service.moveRound(id, row.eventKey(), date(4), date(1)));
        assertEquals(4, root.as.get(1).b());
        verify(state).save(session);
    }

    private CalendarRoundRow event(String phase) {
        return service.getUpcomingRounds(id).stream().filter(row -> phase.equals(row.phase())).findFirst().orElseThrow();
    }

    private static z install(z.Round round) {
        var stage = new z();
        stage.rounds.add(round);
        return stage;
    }

    private Match cupMatch(Competition competition, z stage, z.Round round, int leg) {
        Match match = add(new Match(competition, stage, round.as.get(leg)));
        (leg == 0 ? round.first : round.second).add(match);
        return match;
    }

    private Match add(Match match) {
        var day = root.as.get(match.fy);
        day.h().add(match);
        day.a(match.stage);
        day.a(match.competition);
        return match;
    }

    private static LocalDate date(int index) { return LocalDate.of(2029, 4, 1).plusDays(index); }

    public static class Root {
        public final List<Day> as = IntStream.range(0, 8).mapToObj(Day::new).toList();
        public final List<NationalTeam> nationalTeams = new ArrayList<>();
        public Calendar bb() { return as.get(0).a(); }
        public List<NationalTeam> aG() { return nationalTeams; }
    }

    public static class Day {
        private final int index;
        public int w;
        private final List<Match> matches = new ArrayList<>();
        private final List<Stage> stages = new ArrayList<>();
        private final List<Competition> competitions = new ArrayList<>();

        Day(int index) { this.index = index; }
        public Calendar a() { return GregorianCalendar.from(date(index).atStartOfDay(ZoneOffset.UTC)); }
        public int b() { return w; }
        public boolean e() { return index == 0; }
        public List<Match> h() { return matches; }
        public List<Stage> j() { return stages; }
        public List<Competition> t() { return competitions; }
        public void a(Stage value) { if (!stages.contains(value)) stages.add(value); }
        public void a(Competition value) { if (!competitions.contains(value)) competitions.add(value); }
    }

    public static class Stage {}

    public static class Competition {
        private final String name;
        private final int kind;

        public Competition(String name, int kind) { this.name = name; this.kind = kind; }
        public String getNome() { return name; }
        public int b() { return kind; }
    }

    public record CountryData(int id) {
        public int jc() { return id; }
    }

    public record NationalTeam(Club club) {
        public Club jn() { return club; }
    }

    public static class Club {
        public boolean mW;
        public String dm = "Clube";
    }

    public static class Match {
        private int fy;
        private final Competition competition;
        private final Stage stage;
        private Club home = new Club();
        private final Club away = new Club();

        Match(Competition competition, Stage stage, int fy) {
            this.competition = competition;
            this.stage = stage;
            this.fy = fy;
        }

        public Competition hy() { return competition; }
        public Stage ht() { return stage; }
        public Club hc() { return home; }
        public Club hd() { return away; }
        public boolean e() { return fy == 0; }
        public int hM() { return fy; }
    }
}
