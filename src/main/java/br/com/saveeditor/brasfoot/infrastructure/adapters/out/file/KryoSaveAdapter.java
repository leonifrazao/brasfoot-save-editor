package br.com.saveeditor.brasfoot.infrastructure.adapters.out.file;

import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
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
            NavegacaoState state = saveFileService.restoreFromSnapshot(payload, "service-load");
            SaveContext context = new SaveContext();
            context.load(state, "service-load");
            return context;
        } catch (Exception e) {
            if (isMissingBrasfootClass(e)) {
                throw new IllegalStateException("Brasfoot game classes were not loaded. Check whether lib/brasfoot-game.jar is on the runtime classpath.", e);
            }

            throw new IllegalArgumentException("Invalid save file payload: " + getRootCauseMessage(e), e);
        }
    }

    private boolean isMissingBrasfootClass(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ClassNotFoundException && current.getMessage() != null
                    && current.getMessage().startsWith("best.")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    private String getRootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() != null ? current.getMessage() : current.getClass().getSimpleName();
    }

    @Override
    public byte[] write(SaveContext context) {
        if (!context.isLoaded()) {
            throw new IllegalStateException("Cannot write empty context");
        }
        return saveFileService.createSnapshot(context.getState());
    }
}
