package br.com.saveeditor.brasfoot.adapters.out.cache;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CaffeineSessionAdapter implements SessionStatePort {

    private final Cache<UUID, Session> sessionCache;

    public CaffeineSessionAdapter() {
        this.sessionCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public void save(Session session) {
        if (session != null && session.id() != null) {
            sessionCache.put(session.id(), session);
        }
    }

    @Override
    public Optional<Session> load(UUID id) {
        return Optional.ofNullable(sessionCache.getIfPresent(id));
    }

    @Override
    public void delete(UUID id) {
        sessionCache.invalidate(id);
    }
}