package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SessionResolver {

    private final SessionStatePort sessionStatePort;

    public SessionResolver(SessionStatePort sessionStatePort) {
        this.sessionStatePort = sessionStatePort;
    }

    public UUID parse(String sessionId) {
        try {
            return UUID.fromString(sessionId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid session ID format", e);
        }
    }

    public Session loadRequired(UUID sessionId) {
        Session session = sessionStatePort.load(sessionId);
        if (session.getContext().isLoaded()) {
            throw new IllegalStateException("Session is not loaded");
        }
        return session;
    }
}
