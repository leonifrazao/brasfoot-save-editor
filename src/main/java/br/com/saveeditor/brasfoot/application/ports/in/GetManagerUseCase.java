package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Manager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GetManagerUseCase {
    List<Manager> getManagers(UUID sessionId);
    Optional<Manager> getManager(UUID sessionId, int managerId);
}
