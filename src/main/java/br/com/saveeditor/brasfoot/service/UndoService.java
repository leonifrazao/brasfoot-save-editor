package br.com.saveeditor.brasfoot.service;

import br.com.saveeditor.brasfoot.model.NavegacaoState;
import br.com.saveeditor.brasfoot.util.ConsoleHelper;
import org.springframework.stereotype.Service;

import java.util.Stack;

@Service
public class UndoService {

    private final SaveFileService saveFileService;
    private final Stack<Snapshot> undoStack = new Stack<>();
    private final Stack<Snapshot> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 10;

    public UndoService(SaveFileService saveFileService) {
        this.saveFileService = saveFileService;
    }

    public void captureSnapshot(NavegacaoState currentState, java.util.List<String> currentChangeLog) {
        if (currentState == null)
            return;

        // Create Snapshot
        byte[] stateData = saveFileService.createSnapshot(currentState);
        Snapshot snapshot = new Snapshot(stateData, new java.util.ArrayList<>(currentChangeLog));

        undoStack.push(snapshot);
        // Cap size
        if (undoStack.size() > MAX_HISTORY) {
            undoStack.remove(0);
        }

        redoStack.clear();
    }

    public Snapshot undo(NavegacaoState currentState, java.util.List<String> currentChangeLog) {
        if (undoStack.isEmpty()) {
            ConsoleHelper.warning("Nothing to undo.");
            return null;
        }

        // Capture current to REDO
        byte[] stateData = saveFileService.createSnapshot(currentState);
        Snapshot currentSnapshot = new Snapshot(stateData, new java.util.ArrayList<>(currentChangeLog));
        redoStack.push(currentSnapshot);

        return undoStack.pop();
    }

    public Snapshot redo(NavegacaoState currentState, java.util.List<String> currentChangeLog) {
        if (redoStack.isEmpty()) {
            ConsoleHelper.warning("Nothing to redo.");
            return null;
        }

        // Capture current to UNDO
        byte[] stateData = saveFileService.createSnapshot(currentState);
        Snapshot currentSnapshot = new Snapshot(stateData, new java.util.ArrayList<>(currentChangeLog));
        undoStack.push(currentSnapshot);

        return redoStack.pop();
    }

    public NavegacaoState restoreState(Snapshot snapshot, String originalPath) {
        return saveFileService.restoreFromSnapshot(snapshot.stateData, originalPath);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    public static class Snapshot {
        public final byte[] stateData;
        public final java.util.List<String> changeLog;

        public Snapshot(byte[] stateData, java.util.List<String> changeLog) {
            this.stateData = stateData;
            this.changeLog = changeLog;
        }
    }
}
