package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SessionService implements UploadSaveUseCase, DownloadSaveUseCase {

    private final LoadSavePort loadSavePort;
    private final WriteSavePort writeSavePort;
    private final SessionStatePort sessionStatePort;

    public SessionService(LoadSavePort loadSavePort, WriteSavePort writeSavePort, SessionStatePort sessionStatePort) {
        this.loadSavePort = loadSavePort;
        this.writeSavePort = writeSavePort;
        this.sessionStatePort = sessionStatePort;
    }

    @Override
    public String upload(byte[] payload) {
        SaveContext context = loadSavePort.load(payload);
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, context);
        sessionStatePort.save(session);
        return sessionId.toString();
    }

    @Override
    public byte[] download(String sessionId) {
        UUID id;
        try {
            id = UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session ID format");
        }
        
        Session session = sessionStatePort.load(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found or expired"));
                
        return writeSavePort.write(session.context());
    }
}