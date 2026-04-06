package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.domain.Manager;

import java.util.List;

public interface BatchUpdateManagerUseCase {
    List<Manager> batchUpdateManagers(String sessionId, List<ManagerBatchUpdateCommand> commands);
}
