package br.com.saveeditor.brasfoot.adapters.out.file;

import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.service.SaveFileService;
import br.com.saveeditor.brasfoot.shell.EditorShellContext;
import org.springframework.stereotype.Component;

@Component
public class KryoSaveAdapter implements LoadSavePort, WriteSavePort {

    private final SaveFileService saveFileService;

    public KryoSaveAdapter(SaveFileService saveFileService) {
        this.saveFileService = saveFileService;
    }

    @Override
    public EditorShellContext load(byte[] payload) {
        try {
            NavegacaoState state = saveFileService.restoreFromSnapshot(payload, "api-upload");
            EditorShellContext context = new EditorShellContext();
            context.load(state, "api-upload");
            return context;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid save file payload", e);
        }
    }

    @Override
    public byte[] write(EditorShellContext context) {
        if (!context.isLoaded() || context.getState() == null) {
            throw new IllegalStateException("Cannot write empty context");
        }
        return saveFileService.createSnapshot(context.getState());
    }
}