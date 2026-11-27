package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import br.com.saveeditor.brasfoot.domain.NavegacaoState;
import br.com.saveeditor.brasfoot.domain.SaveContext;
import br.com.saveeditor.brasfoot.infrastructure.adapters.out.state.CurrentSessionAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionServiceTest {
    @TempDir private Path directory;
    private final CurrentSessionAdapter state = new CurrentSessionAdapter();
    private final SessionService service = new SessionService(this::load, this::write, state, new SessionResolver(state));
    private Runnable whenEncoding = () -> {};
    private Path source;
    private Path metadata;
    private UUID id;

    @BeforeEach
    void openSave() throws IOException {
        source = Files.writeString(directory.resolve("season.s22"), "old");
        metadata = Files.writeString(directory.resolve("season.info"), "details");
        id = UUID.fromString(service.open(source));
    }

    @Test
    void stagesSaveThenOverwritesAndRetainsEachPreviousVersion() throws IOException {
        edit("first");
        whenEncoding = () -> assertDoesNotThrow(() -> {
            assertEquals("old", Files.readString(source));
            try (var files = Files.list(directory)) {
                assertTrue(files.anyMatch(path -> path.toString().endsWith(".tmp")));
            }
        });
        assertEquals(source, service.saveOverCurrent(id.toString()));
        assertEquals("first", Files.readString(source));
        whenEncoding = () -> {};
        edit("second");
        service.saveOverCurrent(id.toString());

        assertEquals("second", Files.readString(source));
        assertEquals("details", Files.readString(metadata));
        try (var backups = Files.list(directory.resolve("backups"))) {
            List<Path> saves = backups.filter(path -> path.toString().endsWith(".s22")).toList();
            assertEquals(2, saves.size());
            Set<String> versions = new HashSet<>();
            for (Path save : saves) {
                versions.add(Files.readString(save));
                assertEquals("details", Files.readString(AtomicSaveFiles.metadataPath(save)));
            }
            assertEquals(Set.of("old", "first"), versions);
        }
        assertNoTemporaryFiles();
    }

    @Test
    void writesCopyWithMetadataAndKeepsOriginalCheckpoint() throws IOException {
        edit("copy");
        Path copy = service.saveCopy(id.toString());

        assertEquals("old", Files.readString(source));
        assertEquals("copy", Files.readString(copy));
        assertEquals("details", Files.readString(AtomicSaveFiles.metadataPath(copy)));
        assertEquals(1, service.savePreview(id).steps().size());
        assertFalse(Files.exists(directory.resolve("backups")));
        assertNoTemporaryFiles();
    }

    @Test
    void skipsOverwriteWhenBackupCannotBeCreated() throws IOException {
        edit("new");
        Files.writeString(directory.resolve("backups"), "blocker");

        assertThrows(IllegalStateException.class, () -> service.saveOverCurrent(id.toString()));
        assertEquals("old", Files.readString(source));
        assertTrue(service.historyStatus(id).pending());
        assertEquals("blocker", Files.readString(directory.resolve("backups")));
        assertNoTemporaryFiles();
    }

    @Test
    void preservesOriginalAndClearsTempAfterWriteFailure() throws IOException {
        edit("new");
        whenEncoding = () -> { throw new IllegalStateException("Encoding failed"); };

        assertThrows(IllegalStateException.class, () -> service.saveOverCurrent(id.toString()));
        assertEquals("old", Files.readString(source));
        assertTrue(service.historyStatus(id).pending());
        assertFalse(Files.exists(directory.resolve("backups")));
        assertNoTemporaryFiles();
    }

    @Test
    void previewsChangesRelativeToLastOverwriteAcrossUndoAndNewBranch() {
        assertFalse(service.historyStatus(id).canUndo());
        edit("old");
        assertTrue(service.savePreview(id).steps().isEmpty());
        assertFalse(service.historyStatus(id).canUndo());
        SaveEdit savedEdit = edit("first");
        service.saveOverCurrent(id.toString());
        assertTrue(service.savePreview(id).steps().isEmpty());

        assertTrue(service.undo(id));
        assertEquals("old", root().toString());
        assertEquals(List.of(new SaveEdit.Step(savedEdit, true)), service.savePreview(id).steps());
        assertTrue(service.redo(id));
        assertEquals("first", root().toString());
        assertFalse(service.historyStatus(id).pending());

        assertTrue(service.undo(id));
        SaveEdit newEdit = edit("different");
        assertEquals(List.of(new SaveEdit.Step(savedEdit, true), new SaveEdit.Step(newEdit, false)), service.savePreview(id).steps());
        assertFalse(service.redo(id));
        UUID reopened = UUID.fromString(service.open(source));
        assertEquals(new SaveEdit.HistoryStatus(false, false, false), service.historyStatus(reopened));
    }

    @Test
    void rollsBackPartiallyAppliedEditAndPreservesRedo() {
        edit("new");
        assertTrue(service.undo(id));
        assertThrows(IllegalArgumentException.class, () -> service.applyEdit(id, () -> {
            root().replace(0, root().length(), "partial");
            throw new IllegalArgumentException("Edit failed");
        }));
        assertEquals("old", root().toString());
        assertFalse(service.historyStatus(id).pending());
        assertTrue(service.redo(id));
        assertEquals("new", root().toString());
    }

    private SaveEdit edit(String value) {
        return service.applyEdit(id, () -> {
            StringBuilder root = root();
            String before = root.toString();
            root.replace(0, root.length(), value);
            return new SaveEdit("Teste", List.of(new SaveEdit.Detail("Valor", before, value)));
        });
    }

    private StringBuilder root() {
        return (StringBuilder) state.load(id).getContext().getState().getObjetoRaiz();
    }

    private SaveContext load(byte[] data) {
        return new SaveContext(new NavegacaoState(new StringBuilder(new String(data, StandardCharsets.UTF_8)), null, null), null);
    }

    private byte[] write(SaveContext context) {
        whenEncoding.run();
        return context.getState().getObjetoRaiz().toString().getBytes(StandardCharsets.UTF_8);
    }

    private void assertNoTemporaryFiles() throws IOException {
        try (var files = Files.walk(directory)) {
            assertFalse(files.anyMatch(path -> path.toString().endsWith(".tmp")));
        }
    }
}
