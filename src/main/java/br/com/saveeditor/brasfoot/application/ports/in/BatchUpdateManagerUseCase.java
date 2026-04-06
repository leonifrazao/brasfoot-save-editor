package br.com.saveeditor.brasfoot.application.ports.in;

import br.com.saveeditor.brasfoot.adapters.in.web.record.out.BatchResponse;
import br.com.saveeditor.brasfoot.application.ports.in.record.ManagerBatchUpdateCommand;
import br.com.saveeditor.brasfoot.domain.Manager;

import java.util.List;

public interface BatchUpdateManagerUseCase {
    BatchResponse<Manager> batchUpdateManagers(String sessionId, List<ManagerBatchUpdateCommand> commands);
}
