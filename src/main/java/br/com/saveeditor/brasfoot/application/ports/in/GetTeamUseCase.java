package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Team;
import java.util.List;
import java.util.UUID;

public interface GetTeamUseCase {
    List<Team> getAllTeams(UUID sessionId);
    Team getTeam(UUID sessionId, int teamId);
}
