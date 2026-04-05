package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.TeamReputation;
import java.util.UUID;

public interface UpdateTeamUseCase {
    Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation);
}
