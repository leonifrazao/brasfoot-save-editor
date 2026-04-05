package br.com.saveeditor.brasfoot.application.ports.out;

import br.com.saveeditor.brasfoot.domain.Session;
import java.util.Optional;
import java.util.UUID;

public interface SessionStatePort {
    void save(Session session);
    Optional<Session> load(UUID id);
    void delete(UUID id);
}