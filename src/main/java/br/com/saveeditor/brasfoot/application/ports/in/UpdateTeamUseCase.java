package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.application.ports.in.record.TeamBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.domain.Team;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;

import java.util.UUID;

public interface UpdateTeamUseCase {
    Team updateTeam(UUID sessionId, int teamId, Long money, TeamReputation reputation);
    
    BatchResponse<Team> batchUpdateTeams(UUID sessionId, java.util.List<TeamBatchUpdateCommand> commands);
}
