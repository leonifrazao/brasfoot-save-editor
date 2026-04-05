package br.com.saveeditor.brasfoot.adapters.out.cache;

import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.domain.Session;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionDeletedException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionExpiredException;
import br.com.saveeditor.brasfoot.domain.exceptions.SessionNotFoundException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class CaffeineSessionAdapter implements SessionStatePort {

    private final Cache<UUID, Session> sessionCache;
    private final Cache<UUID, String> tombstoneCache;

    public CaffeineSessionAdapter() {
        this.tombstoneCache = Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build();
                
        this.sessionCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .removalListener((UUID key, Session session, RemovalCause cause) -> {
                    if (cause == RemovalCause.EXPIRED) {
                        tombstoneCache.put(key, "EXPIRED");
                    }
                })
                .build();
    }

    @Override
    public void save(Session session) {
        if (session != null && session.id() != null) {
            sessionCache.put(session.id(), session);
        }
    }

    @Override
    public Session load(UUID id) {
        Session session = sessionCache.getIfPresent(id);
        if (session != null) {
            return session;
        }

        String tombstone = tombstoneCache.getIfPresent(id);
        if ("DELETED".equals(tombstone)) {
            throw new SessionDeletedException("Session was explicitly deleted (already downloaded)");
        } else if ("EXPIRED".equals(tombstone)) {
            throw new SessionExpiredException("Session has expired");
        } else {
            throw new SessionNotFoundException("Session not found");
        }
    }

    @Override
    public void delete(UUID id) {
        tombstoneCache.put(id, "DELETED");
        sessionCache.invalidate(id);
    }
}