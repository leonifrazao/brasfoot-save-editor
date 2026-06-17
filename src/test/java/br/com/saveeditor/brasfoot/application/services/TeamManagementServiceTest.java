package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TeamManagementServiceTest {

    @Mock
    private SessionStatePort sessionStatePort;

    @Mock
    private GameDataPort gameDataPort;

    @Mock
    private SessionResolver sessionResolver;

    @Mock
    private NavegacaoState navegacaoState;

    @InjectMocks
    private TeamManagementService teamManagementService;

    private UUID sessionId;
    private Session session;
    private Object root;
    private DummyTeam team1;
    private DummyTeam team2;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        root = new Object();
        SaveContext context = new SaveContext();
        context.load(navegacaoState, "save.sav");
        session = new Session(sessionId, context);

        team1 = new DummyTeam(1, "Team 1", 1000L, 2);
        team2 = new DummyTeam(2, "Team 2", 2000L, 3);
    }

    @Test
    void batchUpdateTeams_skipsUnknownTeamIds_andUpdatesKnownOnes() {
        // Arrange
        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionResolver.loadRequired(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);
        when(gameDataPort.getTeamById(root, 999)).thenReturn(null);

        List<TeamBatchUpdateCommand> commands = List.of(
                new TeamBatchUpdateCommand(1, 5000L, TeamReputation.CONTINENTAL),
                new TeamBatchUpdateCommand(999, 9999L, TeamReputation.MUNDIAL)
        );

        // Act
        BatchResponse<Team> updated = teamManagementService.batchUpdateTeams(sessionId, commands);

        // Assert
        assertEquals(2, updated.getResults().size());
        assertEquals(true, updated.getResults().get(0).isSuccess());
        assertEquals(false, updated.getResults().get(1).isSuccess());
        assertEquals(5000L, team1.nb);
        assertEquals(TeamReputation.CONTINENTAL.getValue(), team1.nc);
        verify(sessionStatePort).save(session);
    }

    @Test
    void updateTeam_withNegativeMoney_throwsAndDoesNotSave() {
        // Arrange/Act/Assert
        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionResolver.loadRequired(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamManagementService.updateTeam(sessionId, 1, -1L, null)
        );
        verify(sessionStatePort, never()).save(session);
    }

    @Test
    void updateTeam_withStadiumData_updatesStadiumAndSaves() {
        // Arrange
        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionResolver.loadRequired(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);

        // Act
        Team updated = teamManagementService.updateTeam(
                sessionId,
                1,
                null,
                null,
                "New Stadium",
                List.of(1000, 2000, 3000, 4000)
        );

        // Assert
        assertEquals("New Stadium", team1.dH.dm);
        assertEquals(1000, team1.dH.dn[0]);
        assertEquals(10_000, updated.getStadiumCapacity());
        assertEquals(List.of(1000, 2000, 3000, 4000), updated.getStadiumSectors());
        verify(sessionStatePort).save(session);
    }

    @Test
    void batchUpdateTeams_withLevelAndMinimumStadium_updatesTeam() {
        // Arrange
        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionResolver.loadRequired(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);

        List<TeamBatchUpdateCommand> commands = List.of(
                new TeamBatchUpdateCommand(1, 0L, TeamReputation.MUNICIPAL, null, List.of(1, 0, 0, 0), 0)
        );

        // Act
        BatchResponse<Team> updated = teamManagementService.batchUpdateTeams(sessionId, commands);

        // Assert
        assertEquals(true, updated.getResults().get(0).isSuccess());
        assertEquals(0L, team1.nb);
        assertEquals(TeamReputation.MUNICIPAL.getValue(), team1.nc);
        assertEquals(0, team1.hA);
        assertEquals(List.of(1, 0, 0, 0), updated.getResults().get(0).getData().getStadiumSectors());
        verify(sessionStatePort).save(session);
    }

    @Test
    void getAllTeams_withMissingSession_throws() {
        // Arrange
        UUID missingSessionId = UUID.randomUUID();
        when(sessionResolver.loadRequired(missingSessionId)).thenThrow(new IllegalArgumentException("Session not found"));

        // Act/Assert
        assertThrows(IllegalArgumentException.class, () -> teamManagementService.getAllTeams(missingSessionId));
    }

    @SuppressWarnings("unused")
    private static class DummyTeam {
        public int mU;
        public int na = -1;
        public String dm;
        public long nb;
        public int nc;
        public int hA = 5;
        public DummyStadium dH;

        DummyTeam(int id, String name, long money, int reputation) {
            this.mU = id;
            this.dm = name;
            this.nb = money;
            this.nc = reputation;
            this.dH = new DummyStadium("Old Stadium", new int[]{100, 200, 300, 400});
        }
    }

    @SuppressWarnings("unused")
    private static class DummyStadium {
        public String dm;
        public int[] dn;

        DummyStadium(String name, int[] sectors) {
            this.dm = name;
            this.dn = sectors;
        }
    }
}
