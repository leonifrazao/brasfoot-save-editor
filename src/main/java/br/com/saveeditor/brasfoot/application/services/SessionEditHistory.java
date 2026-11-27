package br.com.saveeditor.brasfoot.application.services;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

final class SessionEditHistory {
    private static final int MAX_UNDO_STEPS = 30;
    private static final long SNAPSHOT_BUDGET = 128L * 1024 * 1024;
    private final Deque<State> undo = new ArrayDeque<>();
    private final Deque<State> redo = new ArrayDeque<>();
    private State current = new State(new Revision(null, null), null);
    private Revision saved = current.revision;
    private byte[] savedFingerprint;
    private long undoBytes;

    Snapshot currentSnapshot() {
        return current.snapshot;
    }

    void initialize(Snapshot baseline) {
        savedFingerprint = baseline.fingerprint;
        current = new State(current.revision, baseline);
    }

    void record(Snapshot snapshot, SaveEdit edit) {
        if (Arrays.equals(current.snapshot.fingerprint, snapshot.fingerprint)) return;
        pushUndo(current, snapshot.size());
        redo.clear();
        current = new State(new Revision(current.revision, edit), snapshot);
    }

    State undoTarget() { return undo.peekLast(); }
    State redoTarget() { return redo.peekLast(); }

    void didUndo() {
        redo.addLast(current);
        current = undo.removeLast();
        undoBytes -= current.snapshot.size();
    }

    void didRedo() {
        State next = redo.removeLast();
        pushUndo(current, next.snapshot.size());
        current = next;
    }

    void markSaved() {
        saved = current.revision;
        savedFingerprint = current.snapshot == null ? null : current.snapshot.fingerprint;
    }

    SaveEdit.HistoryStatus status() {
        return new SaveEdit.HistoryStatus(!undo.isEmpty(), !redo.isEmpty(), pending());
    }

    List<SaveEdit.Step> pendingSteps() {
        if (!pending()) return List.of();
        Set<Revision> ancestors = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Revision rev = current.revision; rev != null; rev = rev.parent) ancestors.add(rev);
        List<SaveEdit.Step> result = new ArrayList<>();
        Revision common = saved;
        while (!ancestors.contains(common)) {
            result.add(new SaveEdit.Step(common.edit, true));
            common = common.parent;
        }
        List<SaveEdit.Step> forward = new ArrayList<>();
        for (Revision rev = current.revision; rev != common; rev = rev.parent) {
            forward.add(new SaveEdit.Step(rev.edit, false));
        }
        Collections.reverse(forward);
        result.addAll(forward);
        return List.copyOf(result);
    }

    private boolean pending() {
        return current.revision != saved && !Arrays.equals(current.snapshot.fingerprint, savedFingerprint);
    }

    private void pushUndo(State state, int nextSnapshotSize) {
        undo.addLast(state);
        undoBytes += state.snapshot.size();
        while (undo.size() > MAX_UNDO_STEPS || (undo.size() > 1 && undoBytes + nextSnapshotSize > SNAPSHOT_BUDGET)) {
            undoBytes -= undo.removeFirst().snapshot.size();
        }
    }

    private record Revision(Revision parent, SaveEdit edit) {}
    record State(Revision revision, Snapshot snapshot) {}

    record Snapshot(byte[] stored, boolean wrapped, byte[] fingerprint) {
        static Snapshot capture(byte[] bytes) {
            byte[] fingerprint;
            try {
                fingerprint = MessageDigest.getInstance("SHA-256").digest(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            if (bytes.length >= 2 && bytes[0] == 0x1f && (bytes[1] & 0xff) == 0x8b) {
                return new Snapshot(bytes, false, fingerprint);
            }
            var output = new ByteArrayOutputStream();
            var deflater = new Deflater(Deflater.BEST_SPEED);
            try (var compressed = new DeflaterOutputStream(output, deflater)) {
                compressed.write(bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                deflater.end();
            }
            return new Snapshot(output.toByteArray(), true, fingerprint);
        }

        byte[] payload() {
            if (!wrapped) return stored;
            try (var input = new InflaterInputStream(new ByteArrayInputStream(stored))) {
                return input.readAllBytes();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        int size() { return stored.length; }
    }
}
