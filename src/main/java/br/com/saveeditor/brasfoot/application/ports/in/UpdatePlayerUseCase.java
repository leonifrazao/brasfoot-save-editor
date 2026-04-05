package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Player;
import java.util.UUID;

public interface UpdatePlayerUseCase {
    Player updatePlayer(UUID sessionId, int teamId, int playerId, Integer age, Integer overall, Integer position, Integer energy, Integer morale);
}
