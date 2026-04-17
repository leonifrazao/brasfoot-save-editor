package br.com.saveeditor.brasfoot.infrastructure.adapters.out.file;

import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.application.shared.NavegacaoState;
import br.com.saveeditor.brasfoot.service.SaveFileService;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import org.springframework.stereotype.Component;

@Component
public class KryoSaveAdapter implements LoadSavePort, WriteSavePort {

    private final SaveFileService saveFileService;

    public KryoSaveAdapter(SaveFileService saveFileService) {
        this.saveFileService = saveFileService;
    }

    @Override
    public SaveContext load(byte[] payload) {
        try {
            NavegacaoState state = saveFileService.restoreFromSnapshot(payload, "api-upload");
            SaveContext context = new SaveContext();
            context.load(state, "api-upload");
            return context;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid save file payload", e);
        }
    }

    @Override
    public byte[] write(SaveContext context) {
        if (context.isLoaded() || context.getState() == null) {
            throw new IllegalStateException("Cannot write empty context");
        }
        return saveFileService.createSnapshot(context.getState());
    }
}