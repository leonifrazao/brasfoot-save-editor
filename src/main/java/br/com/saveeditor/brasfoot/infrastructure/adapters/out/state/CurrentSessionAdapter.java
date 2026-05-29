package br.com.saveeditor.brasfoot.infrastructure.adapters.out.state;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CurrentSessionAdapter implements SessionStatePort {

    private Session currentSession;

    @Override
    public synchronized void save(Session session) {
        if (session != null && session.getId() != null) {
            currentSession = session;
        }
    }

    @Override
    public synchronized Session load(UUID id) {
        if (currentSession != null && currentSession.getId().equals(id)) {
            return currentSession;
        }

        throw new SessionNotFoundException("No save is currently loaded");
    }

    @Override
    public void delete(UUID id) {
        // Desktop flow keeps the current save loaded after writing copies.
    }
}
