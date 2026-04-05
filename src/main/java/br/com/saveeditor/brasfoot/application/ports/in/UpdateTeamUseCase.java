package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Team;
import java.util.UUID;

public interface UpdateTeamUseCase {
    Team updateTeam(UUID sessionId, int teamId, Long money, Integer reputation);
}
