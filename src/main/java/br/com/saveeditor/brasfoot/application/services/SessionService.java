package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService implements UploadSaveUseCase, DownloadSaveUseCase {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final LoadSavePort loadSavePort;
    private final WriteSavePort writeSavePort;
    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;

    public SessionService(LoadSavePort loadSavePort, WriteSavePort writeSavePort, SessionStatePort sessionStatePort,
                          SessionResolver sessionResolver) {
        this.loadSavePort = loadSavePort;
        this.writeSavePort = writeSavePort;
        this.sessionStatePort = sessionStatePort;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public String upload(byte[] payload) {
        log.debug("Loading save payload, size: {} bytes", payload != null ? payload.length : 0);
        SaveContext context = loadSavePort.load(payload);
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, context);
        sessionStatePort.save(session);
        log.info("Session created: {}", sessionId);
        return sessionId.toString();
    }

    @Override
    public byte[] download(String sessionId) {
        UUID id = sessionResolver.parse(sessionId);
        Session session = sessionResolver.loadRequired(id);
        
        log.debug("Writing save payload for session: {}", id);
        byte[] payload = writeSavePort.write(session.getContext());
        sessionStatePort.delete(id);
        log.info("Session downloaded and deleted: {}", id);
        return payload;
    }
}
