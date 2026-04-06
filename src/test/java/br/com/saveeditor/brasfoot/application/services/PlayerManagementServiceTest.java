package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.ports.out.GameDataPort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerManagementServiceTest {

    @Mock
    private SessionStatePort sessionStatePort;

    @Mock
    private GameDataPort gameDataPort;

    @Mock
    private NavegacaoState navegacaoState;

    @InjectMocks
    private PlayerManagementService playerManagementService;

    private UUID sessionId;
    private Session session;
    private Object root;
    private Object team;
    private List<Object> players;
    private DummyPlayer player0;
    private DummyPlayer player1;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        root = new Object();
        team = new Object();

        SaveContext context = new SaveContext();
        context.load(navegacaoState, "save.sav");
        session = new Session(sessionId, context);

        player0 = new DummyPlayer("P0", 20, 70, 2, 80);
        player1 = new DummyPlayer("P1", 22, 75, 3, 85);
        players = new ArrayList<>();
        players.add(player0);
        players.add(player1);

        when(navegacaoState.getObjetoRaiz()).thenReturn(root);
        when(sessionStatePort.load(sessionId)).thenReturn(session);
        when(gameDataPort.getTeamById(root, 10)).thenReturn(team);
        when(gameDataPort.getPlayers(team)).thenReturn(players);
    }

    @Test
    void updatePlayer_invalidEnergy_throwsAndDoesNotSave() {
        // Arrange/Act/Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> playerManagementService.updatePlayer(sessionId, 10, 0, null, null, null, -2, null)
        );
        verify(sessionStatePort, never()).save(session);
    }

    @Test
    void batchUpdatePlayers_skipsOutOfBoundsAndUpdatesValid() {
        // Arrange
        List<PlayerBatchUpdateCommand> commands = List.of(
                new PlayerBatchUpdateCommand(0, 25, null, null, null, null),
                new PlayerBatchUpdateCommand(99, 30, null, null, null, null)
        );

        // Act
        List<Player> updated = playerManagementService.batchUpdatePlayers(sessionId, 10, commands);

        // Assert
        assertEquals(1, updated.size());
        assertEquals(25, player0.em);
        verify(sessionStatePort).save(session);
    }

    @Test
    void getPlayer_outOfBounds_throwsIllegalArgumentException() {
        // Arrange/Act/Assert
        assertThrows(IllegalArgumentException.class, () -> playerManagementService.getPlayer(sessionId, 10, 5));
    }

    @SuppressWarnings("unused")
    private static class DummyPlayer {
        public String dm;
        public int em;
        public int eq;
        public int en;
        public int ep;

        DummyPlayer(String name, int age, int overall, int position, int energy) {
            this.dm = name;
            this.em = age;
            this.eq = overall;
            this.en = position;
            this.ep = energy;
        }
    }
}
