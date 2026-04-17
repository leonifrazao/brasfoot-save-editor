package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.application.shared.BatchResponse;
import br.com.saveeditor.brasfoot.domain.Manager;

import java.util.List;
import java.util.UUID;

public interface BatchUpdateManagerUseCase {
    BatchResponse<Manager> batchUpdateManagers(UUID sessionId, List<ManagerBatchUpdateCommand> commands);
}
