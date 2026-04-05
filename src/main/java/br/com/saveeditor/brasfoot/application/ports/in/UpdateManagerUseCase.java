package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Manager;

public interface UpdateManagerUseCase {
    Manager updateManager(String sessionId, int managerId, Manager updateData);
}
