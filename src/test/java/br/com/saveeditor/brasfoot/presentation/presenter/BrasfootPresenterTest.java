package br.com.saveeditor.brasfoot.presentation.presenter;

import br.com.saveeditor.brasfoot.application.services.*;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.view.BrasfootDesktopView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BrasfootPresenterTest {
    private final UUID sessionId = UUID.randomUUID();
    private final TeamManagementService teams = mock(TeamManagementService.class);
    private final ManagerManagementService managers = mock(ManagerManagementService.class);
    private final PlayerManagementService players = mock(PlayerManagementService.class);
    private final BrasfootDesktopView view = mock(BrasfootDesktopView.class);
    private BrasfootPresenter presenter;

    @BeforeEach
    void openSave() {
        SessionService sessions = mock(SessionService.class);
        Path save = Path.of("fixture.s22");
        when(sessions.open(save)).thenReturn(sessionId.toString());
        when(sessions.applyEdit(eq(sessionId), any())).thenAnswer(call -> call.<Supplier<SaveEdit>>getArgument(1).get());
        when(teams.getAllTeams(sessionId)).thenReturn(List.of(team(1), team(2), team(3)));
        presenter = new BrasfootPresenter(sessions, teams, players, managers, mock(LeagueManagementService.class),
                mock(CountryManagementService.class), mock(CalendarManagementService.class), null);
        presenter.attach(view);
        presenter.openSave(save);
    }

    @Test
    void destroyTeamsPreservesEveryClubManagedByHumans() {
        when(managers.getManagers(sessionId)).thenReturn(List.of(manager(0, true, 1),
                manager(1, true, 2), manager(2, false, 3)));
        when(teams.batchUpdateTeams(eq(sessionId), anyList())).thenReturn(new BatchResponse<>(List.of()));
        when(players.batchUpdatePlayers(eq(sessionId), eq(3), anyList())).thenReturn(new BatchResponse<>(List.of()));

        presenter.destroyTeams(true);

        verify(teams).batchUpdateTeams(eq(sessionId),
                argThat(commands -> commands.size() == 1 && commands.get(0).teamId() == 3));
        verify(players).batchUpdatePlayers(eq(sessionId), eq(3), anyList());
        verify(players, times(1)).batchUpdatePlayers(eq(sessionId), anyInt(), anyList());
    }

    @Test
    void destroyTeamsRequiresAHumanManagerWithAClubToProtect() {
        when(managers.getManagers(sessionId)).thenReturn(List.of(manager(0, true, -1), manager(1, true, 42)));

        presenter.destroyTeams(true);

        verify(view).showError(eq("Time humano não encontrado"), anyString());
        verify(teams, never()).batchUpdateTeams(any(), anyList());
    }

    private static Team team(int id) {
        return new Team(id, "Time " + id, 100, TeamReputation.MUNICIPAL);
    }

    private static Manager manager(int id, boolean human, int teamId) {
        return Manager.of(id, "Técnico " + id, human, teamId, 95, 85);
    }
}
