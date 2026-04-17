package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Manager;

import java.util.UUID;

public interface UpdateManagerUseCase {
    Manager updateManager(UUID sessionId, int managerId, Manager updateData);
}
