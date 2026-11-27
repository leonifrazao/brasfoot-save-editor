package br.com.saveeditor.brasfoot.application.shared;

import java.nio.file.Path;
import java.util.List;

public record SaveEdit(String title, List<Detail> details) {
    public SaveEdit {
        details = List.copyOf(details);
    }

    public record Detail(String field, String before, String after) {}
    public record Step(SaveEdit edit, boolean reversed) {}
    public record Preview(Path source, List<Step> steps) {}
    public record HistoryStatus(boolean canUndo, boolean canRedo, boolean pending) {}
}
