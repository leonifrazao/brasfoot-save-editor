package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.domain.PlayerExtraAttributes;
import br.com.saveeditor.brasfoot.domain.enums.TeamReputation;
import br.com.saveeditor.brasfoot.presentation.model.PlayerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import com.formdev.flatlaf.FlatLightLaf;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SideEditorTest {
    @Test
    void spinnerReadsTypedValuesBeforeCommitAndKeepsTheBatchSentinel() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            NumericSpinner batch = new NumericSpinner("Idade", -1, -1, 50);
            assertEquals(-1, batch.readInt(false));
            batch.input().setText(" 27 ");
            assertEquals(27, batch.readInt(false));
            assertEquals(-1L, batch.getValue());
            assertDoesNotThrow(batch::commitEdit);
            assertEquals(28L, batch.getNextValue());

            batch.input().setText("51");
            assertThrows(IllegalArgumentException.class, () -> batch.readInt(false));
            batch.input().setText("17x");
            assertThrows(IllegalArgumentException.class, () -> batch.readInt(false));
            batch.display(null);
            assertNull(batch.readInt(true));
            assertThrows(IllegalArgumentException.class, () -> batch.readInt(false));

            NumericSpinner money = new NumericSpinner("Dinheiro", 0, 0, Long.MAX_VALUE);
            money.input().setText("5000000000");
            assertEquals(5_000_000_000L, money.read(false));
        });
    }

    @Test
    void draftsSurviveSelectionChangesAndMergeWithFreshRows() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TestEditor editor = new TestEditor();
            JButton apply = button(editor, "Aplicar alterações");
            JButton discard = button(editor, "Descartar alterações");
            editor.showRecord(null, null);
            assertFalse(editor.amount.isEnabled());

            editor.showRecord("1", new Value("Time A", 15));
            editor.amount.input().setText("20");
            assertEquals(1, editor.draftCount());
            assertTrue(apply.isEnabled());

            editor.showRecord("2", new Value("Time B", 40));
            assertEquals(40, editor.amount.readInt(false));
            assertFalse(apply.isEnabled());
            editor.showRecord("1", new Value("Time A atualizado", 16));
            assertEquals("Time A atualizado", editor.name.getText());
            assertEquals(20, editor.amount.readInt(false));

            editor.saveSucceeds = false;
            apply.doClick(0);
            assertEquals(1, editor.draftCount());
            assertTrue(discard.isEnabled());
            discard.doClick(0);
            assertEquals(16, editor.amount.readInt(false));
            assertEquals(0, editor.draftCount());

            editor.amount.input().setText("37");
            editor.saveSucceeds = true;
            apply.doClick(0);
            assertEquals(37, editor.savedAmount);
            assertEquals(0, editor.draftCount());
            assertFalse(apply.isEnabled());
        });
    }

    @Test
    void refreshKeepsSortFilterSelectionAndScroll() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            RecordTable<Integer> table = new RecordTable<>(Integer::intValue,
                    value -> new Object[]{value}, "ID");
            table.setRows(IntStream.range(0, 100).boxed().toList(), true);
            var scroll = table.scrollPane();
            scroll.setSize(240, 140);
            scroll.doLayout();
            table.setSize(210, 1600);
            var selected = new AtomicReference<Integer>();
            table.onSelection(selected::set);
            table.getRowSorter().toggleSortOrder(0);
            table.getRowSorter().toggleSortOrder(0);
            table.filter(value -> value % 2 == 0);
            table.setRowSelectionInterval(20, 20);
            assertEquals(58, selected.get());
            scroll.getViewport().setViewPosition(new Point(0, 180));

            table.setRows(IntStream.range(0, 100).boxed().toList(), false);

            assertEquals(58, selected.get());
            assertEquals(50, table.getRowCount());
            assertEquals(98, table.getValueAt(0, 0));
            assertEquals(new Point(0, 180), scroll.getViewport().getViewPosition());

            table.filter(value -> value > 80);
            assertNull(selected.get());
            table.filter(value -> true);
            assertEquals(58, selected.get());
        });
    }

    @Test
    void teamEditorSubmitsOnlyEditedFieldsIncludingTypedReputation() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BrasfootPresenter presenter = mock(BrasfootPresenter.class);
            TeamRow team = new TeamRow(10, "Time A", "A", 25, "ESTADUAL", 29, 1, 3, "Estádio", 4,
                    List.of(1, 1, 1, 1), 0, 0, 0);
            TeamEditorPanel editor = new TeamEditorPanel(presenter);
            editor.showRecord(team.id(), team);
            var reputation = (JComboBox<?>) inputFor(editor, "Reputação");
            assertEquals(TeamReputation.ESTADUAL, reputation.getSelectedItem());
            assertEquals(TeamReputation.values().length, reputation.getItemCount());
            reputation.setSelectedItem(TeamReputation.CONTINENTAL);
            number(editor, "Dinheiro").input().setText("5000000000");
            button(editor, "Aplicar alterações").doClick(0);

            verify(presenter).updateTeam(10, null, null, 5_000_000_000L, TeamReputation.CONTINENTAL,
                    null, null, null, null, null, null, null, null);
            assertEquals(1, editor.draftCount()); // The presenter reported a failed update.
        });
    }

    @Test
    void playerDraftsAreIsolatedAcrossRostersWithTheSamePlayerIndex() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BrasfootPresenter presenter = mock(BrasfootPresenter.class);
            PlayerRow player = new PlayerRow(0, "Jogador", 20, 70, 1, 80, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, false, false);
            PlayerEditorPanel editor = new PlayerEditorPanel(presenter);
            editor.showRecord(new PlayerEditorPanel.Key(1, 0), player);
            number(editor, "Velocidade").input().setText("85");
            editor.showRecord(new PlayerEditorPanel.Key(2, 0), player);
            assertNull(number(editor, "Velocidade").readInt(true));
            editor.showRecord(new PlayerEditorPanel.Key(1, 0), player);
            assertEquals(85, number(editor, "Velocidade").readInt(true));
            button(editor, "Aplicar alterações").doClick(0);

            verify(presenter).updatePlayer(1, 0, null, null, null, null, null, null, null, null,
                    null, null, null, 85, null, null, null, null, null, null, null, null, PlayerExtraAttributes.EMPTY);
        });
    }

    @Test
    void removeInjuryStagesClearingTheDateUntilThePlayerEditIsApplied() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            BrasfootPresenter presenter = mock(BrasfootPresenter.class);
            long recoveryDate = java.time.Instant.parse("2030-09-15T12:00:00Z").toEpochMilli();
            var attributes = new PlayerExtraAttributes(null, null, null, null, null, null, null, null, recoveryDate);
            var player = new PlayerRow(0, "Jogador", 20, 70, 1, 80, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, false, false, 0, attributes);
            var editor = new PlayerEditorPanel(presenter);
            var key = new PlayerEditorPanel.Key(1, 0);
            editor.showRecord(key, player);
            JButton remove = button(editor, "Remover lesão");
            var date = (JFormattedTextField) inputFor(editor, "Lesionado até");
            JButton apply = button(editor, "Aplicar alterações");

            assertTrue(remove.isEnabled());
            remove.doClick(0);
            assertEquals("", date.getText());
            assertFalse(remove.isEnabled());
            assertTrue(apply.isEnabled());
            verifyNoInteractions(presenter);

            button(editor, "Descartar alterações").doClick(0);
            assertTrue(remove.isEnabled());
            remove.doClick(0);
            apply.doClick(0);

            verify(presenter).updatePlayer(1, 0, null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null, null, null, null,
                    new PlayerExtraAttributes(null, null, null, null, null, null, null, null, 0L));
        });
    }

    @Test
    void playerFormScrollsWithTheActionsInReachAt1366By768() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LookAndFeel previous = UIManager.getLookAndFeel();
            try {
                assertTrue(FlatLightLaf.setup());
                PlayerEditorPanel editor = new PlayerEditorPanel(mock(BrasfootPresenter.class));
                PlayerRow player = new PlayerRow(0, "Jogador", 20, 70, 1, 80, 1000, 0, null, 29,
                        0, 0, 45, 50, 55, 60, 65, 70, 75, false, false);
                editor.showRecord(new PlayerEditorPanel.Key(1, 0), player);
                JSplitPane split = SwingMainWindow.editSplit(new JPanel(), editor);
                // Leave room for the window decorations, tabs and toolbar.
                split.setSize(1320, 640);
                split.setDividerLocation(0.62);
                layout(split);

                JScrollPane form = descendants(editor).filter(JScrollPane.class::isInstance)
                        .map(JScrollPane.class::cast).findFirst().orElseThrow();
                JButton apply = button(editor, "Aplicar alterações");
                JButton discard = button(editor, "Descartar alterações");
                assertTrue(form.getVerticalScrollBar().isVisible());
                assertFalse(form.getHorizontalScrollBar().isVisible());
                assertInside(editor, apply);
                assertInside(editor, discard);

                split.setDividerLocation(1000);
                layout(split);
                assertTrue(editor.getWidth() < 450);
                assertInside(editor, apply);
                assertInside(editor, discard);
            } finally {
                assertDoesNotThrow(() -> UIManager.setLookAndFeel(previous));
            }
        });
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) layout(nested);
        }
    }

    private static void assertInside(JComponent parent, JComponent child) {
        var bounds = SwingUtilities.convertRectangle(child.getParent(), child.getBounds(), parent);
        assertTrue(parent.getVisibleRect().contains(bounds), bounds.toString());
        assertTrue(bounds.width > 0 && bounds.height > 0);
    }

    private static JButton button(Container parent, String text) {
        return descendants(parent).filter(JButton.class::isInstance).map(JButton.class::cast)
                .filter(button -> text.equals(button.getText())).findFirst().orElseThrow();
    }

    private static NumericSpinner number(Container parent, String label) {
        return (NumericSpinner) SwingUtilities.getAncestorOfClass(NumericSpinner.class, inputFor(parent, label));
    }

    private static Component inputFor(Container parent, String label) {
        return descendants(parent).filter(JLabel.class::isInstance).map(JLabel.class::cast)
                .filter(caption -> label.equals(caption.getText())).findFirst().orElseThrow().getLabelFor();
    }

    private static Stream<Component> descendants(Component component) {
        return component instanceof Container container
                ? Stream.concat(Stream.of(component), Arrays.stream(container.getComponents()).flatMap(SideEditorTest::descendants))
                : Stream.of(component);
    }

    private record Value(String name, int amount) {}

    private static final class TestEditor extends RecordEditor<String, Value> {
        private final JTextField name;
        private final NumericSpinner amount;
        private boolean saveSucceeds;
        private int savedAmount;

        TestEditor() {
            super("Selecione um time.");
            JPanel section = group("Time");
            name = field(section, "Nome", new JTextField());
            amount = field(section, "Valor", new NumericSpinner("Valor", 0, 0, 100));
        }

        @Override String recordName(Value record) { return record.name(); }
        @Override void load(Value record) {
            enableFields();
            name.setText(record.name());
            amount.display(record.amount());
        }
        @Override boolean save(String key, Value row) {
            savedAmount = amount.readInt(false);
            if (saveSucceeds) showRecord(key, new Value(name.getText(), savedAmount));
            return saveSucceeds;
        }
    }
}
