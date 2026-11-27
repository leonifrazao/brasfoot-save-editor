package br.com.saveeditor.brasfoot.presentation.presenter;

import br.com.saveeditor.brasfoot.application.services.*;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.Manager;
import br.com.saveeditor.brasfoot.domain.Player;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.Country;
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

    @Test
    void countryPresetTargetsOnlyClubsInTheChosenCountryAndCanPreserveHumanClubs() {
        int brazil = Country.BRASIL.getId();
        Team human = team(1);
        human.setCountry(brazil);
        Team rival = team(2);
        rival.setCountry(brazil);
        Team overseas = team(3);
        overseas.setCountry(Country.PORTUGAL.getId());
        when(teams.getAllTeams(sessionId)).thenReturn(List.of(human, rival, overseas));
        when(managers.getManagers(sessionId)).thenReturn(List.of(manager(0, true, 1)));
        when(teams.batchUpdateTeams(eq(sessionId), anyList())).thenReturn(new BatchResponse<>(List.of()));
        when(players.batchUpdatePlayers(eq(sessionId), anyInt(), anyList())).thenReturn(new BatchResponse<>(List.of()));

        presenter.destroyTeamsInCountry(brazil, true);
        verify(teams).batchUpdateTeams(eq(sessionId),
                argThat(commands -> commands.size() == 1 && commands.get(0).teamId() == 2));
        verify(players, never()).batchUpdatePlayers(eq(sessionId), eq(1), anyList());

        presenter.destroyTeamsInCountry(brazil, false);
        verify(teams).batchUpdateTeams(eq(sessionId),
                argThat(commands -> commands.size() == 2 && commands.get(0).teamId() == 1 && commands.get(1).teamId() == 2));
        verify(players, never()).batchUpdatePlayers(eq(sessionId), eq(3), anyList());
    }

    @Test
    void improvePresetRaisesClubAndPlayerAttributesForEveryIncludedTeam() {
        when(managers.getManagers(sessionId)).thenReturn(List.of(manager(0, true, 1)));
        when(teams.batchUpdateTeams(eq(sessionId), anyList())).thenReturn(new BatchResponse<>(List.of()));
        when(players.getTeamPlayers(eq(sessionId), anyInt()))
                .thenReturn(List.of(new Player(0, "Jogador", 21, 70, 2, 80, 100)));
        when(players.batchUpdatePlayers(eq(sessionId), anyInt(), anyList())).thenReturn(new BatchResponse<>(List.of()));

        presenter.improveTeams(true);

        verify(teams).batchUpdateTeams(eq(sessionId), argThat(commands -> commands.size() == 2
                && commands.stream().allMatch(command -> command.teamId() != 1
                && command.reputation() == TeamReputation.MUNDIAL && command.money() > 100 && command.level() == 100)));
        verify(players, times(2)).batchUpdatePlayers(eq(sessionId), intThat(id -> id != 1),
                argThat(commands -> commands.size() == 1 && commands.get(0).overall() == 100
                        && commands.get(0).energy() == 100 && commands.get(0).skillFinishing() == 100
                        && commands.get(0).starLocal() && commands.get(0).starGlobal()));
    }

    private static Team team(int id) {
        return new Team(id, "Time " + id, 100, TeamReputation.MUNICIPAL);
    }

    private static Manager manager(int id, boolean human, int teamId) {
        return Manager.of(id, "Técnico " + id, human, teamId, 95, 85);
    }
}
