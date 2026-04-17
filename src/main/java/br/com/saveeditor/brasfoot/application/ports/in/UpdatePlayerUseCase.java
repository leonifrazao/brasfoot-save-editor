package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.application.ports.in.record.PlayerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.domain.Player;

import java.util.UUID;

public interface UpdatePlayerUseCase {
    Player updatePlayer(UUID sessionId, int teamId, int playerId, Integer age, Integer overall, Integer position, Integer energy, Integer morale, Boolean starLocal, Boolean starGlobal);
    
    BatchResponse<Player> batchUpdatePlayers(UUID sessionId, int teamId, java.util.List<PlayerBatchUpdateCommand> commands);
}
