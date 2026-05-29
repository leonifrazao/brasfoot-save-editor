package br.com.saveeditor.brasfoot.presentation.view;

import br.com.saveeditor.brasfoot.presentation.model.LeagueRow;
import br.com.saveeditor.brasfoot.presentation.model.LeagueTableRow;
import br.com.saveeditor.brasfoot.presentation.model.CountryRow;
import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;

import java.nio.file.Path;
import java.util.List;

public interface BrasfootDesktopView {
    void showSessionOpened(String sessionId, Path savePath);

    void showSaveWritten(Path outputPath);

    void showTeams(List<TeamRow> teams);

    void showPlayers(int teamId, List<PlayerRow> players);

    void showManagers(List<ManagerRow> managers);

    void showLeagues(List<LeagueRow> leagues);

    void showLeagueTable(String leagueId, List<LeagueTableRow> table);

    void showCountries(List<CountryRow> countries);

    void showStatus(String message);

    void showError(String title, String message);
}
