package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Manager;
import java.util.List;
import java.util.Optional;

public interface GetManagerUseCase {
    List<Manager> getManagers(String sessionId);
    Optional<Manager> getManager(String sessionId, int managerId);
}
