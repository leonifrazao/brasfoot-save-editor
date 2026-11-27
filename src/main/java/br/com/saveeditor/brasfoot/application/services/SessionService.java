package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.ports.in.DownloadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.in.UploadSaveUseCase;
import br.com.saveeditor.brasfoot.application.ports.out.LoadSavePort;
import br.com.saveeditor.brasfoot.application.ports.out.SessionStatePort;
import br.com.saveeditor.brasfoot.application.ports.out.WriteSavePort;
import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.domain.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class SessionService implements UploadSaveUseCase, DownloadSaveUseCase {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);
    private static final DateTimeFormatter BACKUP_TIME = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    private final LoadSavePort loadSavePort;
    private final WriteSavePort writeSavePort;
    private final SessionStatePort sessionStatePort;
    private final SessionResolver sessionResolver;
    private SessionEditHistory history = new SessionEditHistory();

    public SessionService(LoadSavePort loadSavePort, WriteSavePort writeSavePort, SessionStatePort sessionStatePort,
                          SessionResolver sessionResolver) {
        this.loadSavePort = loadSavePort;
        this.writeSavePort = writeSavePort;
        this.sessionStatePort = sessionStatePort;
        this.sessionResolver = sessionResolver;
    }

    @Override
    public synchronized String upload(byte[] payload) {
        log.debug("Loading save payload, size: {} bytes", payload != null ? payload.length : 0);
        SaveContext context = loadSavePort.load(payload);
        UUID sessionId = UUID.randomUUID();
        Session session = new Session(sessionId, context);
        sessionStatePort.save(session);
        history = new SessionEditHistory();
        log.info("Session created: {}", sessionId);
        return sessionId.toString();
    }

    public synchronized String open(Path savePath) {
        try {
            Path absolutePath = savePath.toAbsolutePath().normalize();
            SaveContext context = loadSavePort.load(Files.readAllBytes(absolutePath));
            context.setCurrentFilePath(absolutePath.toString());
            UUID sessionId = UUID.randomUUID();
            sessionStatePort.save(new Session(sessionId, context));
            history = new SessionEditHistory();
            log.info("Save opened from {} with session {}", absolutePath, sessionId);
            return sessionId.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read save file: " + savePath, e);
        }
    }

    @Override
    public synchronized byte[] download(String sessionId) {
        UUID id = sessionResolver.parse(sessionId);
        Session session = sessionResolver.loadRequired(id);
        
        log.debug("Writing save payload for session: {}", id);
        byte[] payload = writeSavePort.write(session.getContext());
        return payload;
    }

    public synchronized SaveEdit applyEdit(UUID sessionId, Supplier<SaveEdit> operation) {
        Session session = sessionResolver.loadRequired(sessionId);
        if (history.currentSnapshot() == null) {
            history.initialize(snapshot(session));
        }
        String path = session.getContext().getCurrentFilePath();
        try {
            SaveEdit edit = operation.get();
            history.record(snapshot(session), edit);
            return edit;
        } catch (RuntimeException error) {
            try {
                restore(session, history.currentSnapshot(), path);
            } catch (RuntimeException rollbackError) {
                error.addSuppressed(rollbackError);
            }
            throw error;
        }
    }

    public synchronized boolean undo(UUID id) {
        Session session = sessionResolver.loadRequired(id);
        var target = history.undoTarget();
        if (target == null) return false;
        restore(session, target.snapshot(), session.getContext().getCurrentFilePath());
        history.didUndo();
        return true;
    }

    public synchronized boolean redo(UUID id) {
        Session session = sessionResolver.loadRequired(id);
        var target = history.redoTarget();
        if (target == null) return false;
        restore(session, target.snapshot(), session.getContext().getCurrentFilePath());
        history.didRedo();
        return true;
    }

    public synchronized SaveEdit.HistoryStatus historyStatus(UUID id) {
        sessionResolver.loadRequired(id);
        return history.status();
    }

    public synchronized SaveEdit.Preview savePreview(UUID id) {
        Session session = sessionResolver.loadRequired(id);
        return new SaveEdit.Preview(currentPath(session), history.pendingSteps());
    }

    public synchronized void close(UUID id) {
        sessionStatePort.delete(id);
        history = new SessionEditHistory();
    }

    private SessionEditHistory.Snapshot snapshot(Session session) {
        return SessionEditHistory.Snapshot.capture(writeSavePort.write(session.getContext()));
    }

    private void restore(Session session, SessionEditHistory.Snapshot snapshot, String path) {
        SaveContext restored = loadSavePort.load(snapshot.payload());
        restored.setCurrentFilePath(path);
        session.setContext(restored);
        sessionStatePort.save(session);
    }

    public synchronized Path saveCopy(String sessionId) {
        Session session = sessionResolver.loadRequired(sessionResolver.parse(sessionId));
        Path source = currentPath(session);
        Path output = source.resolveSibling("brasfoot-save-" + uniqueId() + extensionOf(source.getFileName().toString()));
        try {
            AtomicSaveFiles.write(output, () -> writeSavePort.write(session.getContext()), AtomicSaveFiles.metadataPath(source));
            log.info("Save copy written to {}", output);
            return output;
        } catch (AtomicMoveNotSupportedException e) {
            throw new IllegalStateException("O sistema de arquivos não permite publicar a cópia de forma atômica.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gravar a cópia: " + output, e);
        }
    }

    public synchronized Path saveOverCurrent(String sessionId) {
        Session session = sessionResolver.loadRequired(sessionResolver.parse(sessionId));
        Path output = currentPath(session);
        try {
            AtomicSaveFiles.replace(output, () -> writeSavePort.write(session.getContext()), () -> backupCurrent(output));
            history.markSaved();
            log.info("Save replaced at {}", output);
            return output;
        } catch (AtomicMoveNotSupportedException e) {
            throw new IllegalStateException("O sistema de arquivos não permite substituir o save de forma atômica.", e);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível substituir o save: " + output, e);
        }
    }

    private void backupCurrent(Path source) {
        try {
            Path directory = Files.createDirectories(source.getParent().resolve("backups"));
            String filename = source.getFileName().toString();
            int extension = filename.lastIndexOf('.');
            String base = extension > 0 ? filename.substring(0, extension) : filename;
            Path backup = directory.resolve(base + "-" + BACKUP_TIME.format(LocalDateTime.now()) + "-"
                    + uniqueId() + extensionOf(filename));
            byte[] existingSave = Files.readAllBytes(source);
            AtomicSaveFiles.write(backup, () -> existingSave, AtomicSaveFiles.metadataPath(source));
            log.info("Backup written to {}", backup);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar o backup. O save original foi preservado.", e);
        }
    }

    private Path currentPath(Session session) {
        String currentPath = session.getContext().getCurrentFilePath();
        if (currentPath == null || currentPath.isBlank()) {
            throw new IllegalStateException("O save atual não tem um caminho de arquivo.");
        }
        return Path.of(currentPath).toAbsolutePath().normalize();
    }

    private String uniqueId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String extensionOf(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return ".s22";
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }
}
