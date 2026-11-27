package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.Component;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ManagerTablePanelTest {
    @Test
    void filtersAsSearchChangesAndCombinesNameOrTeamWithManagerType() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ManagerTablePanel panel = createPanel();
            JTable table = find(panel, JTable.class);
            JTextField search = find(panel, JTextField.class);
            JComboBox<?> type = find(panel, JComboBox.class);

            assertEquals("Humano", table.getColumnName(3));
            assertEquals(Boolean.class, table.getColumnClass(3));
            assertRows(panel, table, 11, 2, 7);
            search.setText("  JOaO ");
            assertRows(panel, table, 11);
            search.setText("sAo PAULO");
            assertRows(panel, table, 11, 2);
            type.setSelectedItem("Humanos");
            assertRows(panel, table, 11);
            type.setSelectedItem("Computador");
            assertRows(panel, table, 2);
            search.setText("sem time");
            assertRows(panel, table);
            search.setText("");
            type.setSelectedItem("Todos");
            assertRows(panel, table, 11, 2, 7);
        });
    }

    @Test
    void maintainsSortAndResolvesTheSelectedManagerDuringFilteringAndRefresh() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            ManagerTablePanel panel = createPanel();
            JTable table = find(panel, JTable.class);
            JTextField search = find(panel, JTextField.class);
            JComboBox<?> type = find(panel, JComboBox.class);

            table.getRowSorter().toggleSortOrder(0);
            search.setText("SAO");
            assertRows(panel, table, 2, 11);
            table.setRowSelectionInterval(1, 1);
            assertEquals(11, panel.selectedManager().id());

            type.setSelectedItem("Humanos");
            assertRows(panel, table, 11);
            assertEquals(11, panel.selectedManager().id());
            panel.setManagers(List.of(manager(7, "Célia", false, null), manager(11, "João", false, 50),
                    manager(2, "Álvaro", true, 51)));
            assertRows(panel, table, 2);
            assertNull(panel.selectedManager());

            type.setSelectedItem("Todos");
            search.setText("");
            assertRows(panel, table, 2, 7, 11);
        });
    }

    private static ManagerTablePanel createPanel() {
        ManagerTablePanel panel = new ManagerTablePanel();
        panel.setTeams(List.of(team(50, "São Paulo"), team(51, "São Paulo B")));
        panel.setManagers(List.of(manager(11, "João", true, 50), manager(2, "Álvaro", false, 51),
                manager(7, "Célia", false, null)));
        return panel;
    }

    private static ManagerRow manager(int id, String name, boolean human, Integer teamId) {
        return new ManagerRow(id, name, human, teamId, 95, 85, List.of(), List.of());
    }

    private static TeamRow team(int id, String name) {
        return new TeamRow(id, name, null, 0, null, null, null, null, null, null, List.of(), null, null, null);
    }

    private static void assertRows(ManagerTablePanel panel, JTable table, Integer... ids) {
        assertEquals(List.of(ids), IntStream.range(0, table.getRowCount()).mapToObj(row -> table.getValueAt(row, 0)).toList());
        String count = ids.length == 1 ? "1 técnico encontrado" : ids.length + " técnicos encontrados";
        assertTrue(components(panel).filter(JLabel.class::isInstance).map(JLabel.class::cast)
                .anyMatch(label -> count.equals(label.getText())));
    }

    private static <T extends Component> T find(Container parent, Class<T> type) {
        return components(parent).filter(type::isInstance).map(type::cast).findFirst().orElseThrow();
    }

    private static Stream<Component> components(Component component) {
        return component instanceof Container container
                ? Stream.concat(Stream.of(component), Arrays.stream(container.getComponents()).flatMap(ManagerTablePanelTest::components))
                : Stream.of(component);
    }
}
