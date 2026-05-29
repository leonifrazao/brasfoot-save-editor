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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
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

    public String open(Path savePath) {
        try {
            Path absolutePath = savePath.toAbsolutePath().normalize();
            SaveContext context = loadSavePort.load(Files.readAllBytes(absolutePath));
            context.setCurrentFilePath(absolutePath.toString());
            UUID sessionId = UUID.randomUUID();
            sessionStatePort.save(new Session(sessionId, context));
            log.info("Save opened from {} with session {}", absolutePath, sessionId);
            return sessionId.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read save file: " + savePath, e);
        }
    }

    @Override
    public byte[] download(String sessionId) {
        UUID id = sessionResolver.parse(sessionId);
        Session session = sessionResolver.loadRequired(id);
        
        log.debug("Writing save payload for session: {}", id);
        byte[] payload = writeSavePort.write(session.getContext());
        return payload;
    }

    public Path saveCopy(String sessionId) {
        UUID id = sessionResolver.parse(sessionId);
        Session session = sessionResolver.loadRequired(id);
        Path outputPath = createRandomOutputPath(session.getContext());

        try {
            Files.write(outputPath, writeSavePort.write(session.getContext()));
            log.info("Save copy written to {}", outputPath);
            return outputPath;
        } catch (IOException e) {
            throw new IllegalStateException("Could not write save copy: " + outputPath, e);
        }
    }

    private Path createRandomOutputPath(SaveContext context) {
        Path sourcePath = Path.of(context.getCurrentFilePath()).toAbsolutePath().normalize();
        Path directory = sourcePath.getParent() != null ? sourcePath.getParent() : Path.of(".").toAbsolutePath().normalize();
        String filename = sourcePath.getFileName().toString();
        String extension = extensionOf(filename);
        String randomName = "brasfoot-save-" + UUID.randomUUID().toString().replace("-", "") + extension;
        return directory.resolve(randomName);
    }

    private String extensionOf(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot < 0 || lastDot == filename.length() - 1) {
            return ".s22";
        }
        return filename.substring(lastDot).toLowerCase(Locale.ROOT);
    }
}
