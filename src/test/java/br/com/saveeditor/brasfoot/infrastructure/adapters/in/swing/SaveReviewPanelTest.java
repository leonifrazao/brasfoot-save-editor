package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveReviewPanelTest {
    @Test
    void showsBeforeAndAfterInTheDirectionThatWillBeWritten() {
        var dateChange = new SaveEdit("Semifinal da copa", List.of(new SaveEdit.Detail("Data", "2029-05-09", "2029-05-10")));
        var playerChange = new SaveEdit("Jogador", List.of(new SaveEdit.Detail("Lado", "Direito", "Esquerdo")));
        var preview = new SaveEdit.Preview(Path.of("fixture.s22"),
                List.of(new SaveEdit.Step(dateChange, true), new SaveEdit.Step(playerChange, false)));

        String text = SaveReviewPanel.reviewText(preview);

        assertTrue(text.contains("fixture.s22"));
        assertTrue(text.contains("Reverter no arquivo · Semifinal da copa"));
        assertTrue(text.contains("2029-05-10 → 2029-05-09"));
        assertTrue(text.contains("Direito → Esquerdo"));
    }
}
