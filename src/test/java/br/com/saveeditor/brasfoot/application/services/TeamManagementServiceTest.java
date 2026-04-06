package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.TeamReputation;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
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
        when(sessionStatePort.load(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);
        when(gameDataPort.getTeamById(root, 999)).thenReturn(null);

        List<TeamBatchUpdateCommand> commands = List.of(
                new TeamBatchUpdateCommand(1, 5000L, TeamReputation.CONTINENTAL),
                new TeamBatchUpdateCommand(999, 9999L, TeamReputation.MUNDIAL)
        );

        // Act
        List<Team> updated = teamManagementService.batchUpdateTeams(sessionId, commands);

        // Assert
        assertEquals(1, updated.size());
        assertEquals(5000L, team1.nb);
        assertEquals(TeamReputation.CONTINENTAL.getValue(), team1.nc);
        verify(sessionStatePort).save(session);
    }

    @Test
    void updateTeam_withNegativeMoney_throwsAndDoesNotSave() {
        // Arrange/Act/Assert
        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionStatePort.load(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 1)).thenReturn(team1);

        assertThrows(
                IllegalArgumentException.class,
                () -> teamManagementService.updateTeam(sessionId, 1, -1L, null)
        );
        verify(sessionStatePort, never()).save(session);
    }

    @Test
    void getAllTeams_withMissingSession_throws() {
        // Arrange
        UUID missingSessionId = UUID.randomUUID();
        when(sessionStatePort.load(missingSessionId)).thenReturn(null);

        // Act/Assert
        assertThrows(IllegalArgumentException.class, () -> teamManagementService.getAllTeams(missingSessionId));
    }

    @SuppressWarnings("unused")
    private static class DummyTeam {
        public int na;
        public String dm;
        public long nb;
        public int nc;

        DummyTeam(int id, String name, long money, int reputation) {
            this.na = id;
            this.dm = name;
            this.nb = money;
            this.nc = reputation;
        }
    }
}
