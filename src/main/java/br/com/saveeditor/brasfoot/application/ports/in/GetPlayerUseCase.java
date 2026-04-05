package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Player;
import java.util.List;
import java.util.UUID;

public interface GetPlayerUseCase {
    List<Player> getTeamPlayers(UUID sessionId, int teamId);
    Player getPlayer(UUID sessionId, int teamId, int playerId);
}
