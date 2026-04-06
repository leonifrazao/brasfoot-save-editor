package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.adapters.in.web.dto.TeamBatchUpdateRequest;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.TeamReputation;
import java.util.List;
import java.util.UUID;

public interface UpdateTeamUseCase {
    Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation);
    
    List<Team> batchUpdateTeams(UUID sessionId, List<TeamBatchUpdateRequest> requests);
}
