package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.presentation.model.ManagerRow;
import br.com.saveeditor.brasfoot.presentation.model.TeamRow;
import br.com.saveeditor.brasfoot.util.StringUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class ManagerTablePanel extends JPanel {
    private final ManagerTableModel model = new ManagerTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<ManagerTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField search = new JTextField(18);
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Todos", "Humanos", "Computador"});
    private final JLabel count = new JLabel();

    ManagerTablePanel() {
        super(new BorderLayout(4, 4));
        table.setRowSorter(sorter);
        sorter.setSortsOnUpdates(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        search.putClientProperty("JTextField.showClearButton", true);
        search.putClientProperty("JTextField.placeholderText", "Nome do técnico ou do time");
        JLabel searchLabel = new JLabel("Pesquisar");
        searchLabel.setLabelFor(search);
        JLabel typeLabel = new JLabel("Tipo");
        typeLabel.setLabelFor(type);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(searchLabel);
        controls.add(search);
        controls.add(typeLabel);
        controls.add(type);
        JPanel top = new JPanel(new BorderLayout());
        top.add(controls, BorderLayout.CENTER);
        top.add(count, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        search.getDocument().addDocumentListener(SwingMainWindow.change(this::filter));
        type.addActionListener(e -> filter());
        updateCount();
    }

    void setTeams(List<TeamRow> teams) {
        model.teamNames.clear();
        for (TeamRow team : teams) {
            model.teamNames.put(team.id(), team.name());
        }
        if (model.getRowCount() > 0) {
            model.fireTableRowsUpdated(0, model.getRowCount() - 1);
        }
        updateCount();
    }

    void setManagers(List<ManagerRow> managers) {
        ManagerRow previous = selectedManager();
        model.managers = List.copyOf(managers);
        model.fireTableDataChanged();
        if (table.getRowCount() > 0) {
            int viewIndex = -1;
            if (previous != null) {
                for (int i = 0; i < managers.size(); i++) {
                    if (managers.get(i).id() == previous.id()) {
                        viewIndex = table.convertRowIndexToView(i);
                        break;
                    }
                }
            }
            if (viewIndex >= 0 || previous == null) {
                int row = Math.max(0, viewIndex);
                table.setRowSelectionInterval(row, row);
            }
        }
        updateCount();
    }

    void addSelectionListener(ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    ManagerRow selectedManager() {
        int row = table.getSelectedRow();
        return row < 0 ? null : model.managers.get(table.convertRowIndexToModel(row));
    }

    private void filter() {
        String query = StringUtils.normalize(search.getText().strip());
        String selectedType = (String) type.getSelectedItem();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ManagerTableModel, ? extends Integer> entry) {
                ManagerRow manager = model.managers.get(entry.getIdentifier());
                boolean human = Boolean.TRUE.equals(manager.human());
                if (("Humanos".equals(selectedType) && !human) || ("Computador".equals(selectedType) && human)) {
                    return false;
                }
                return query.isEmpty() || matches(manager.name(), query)
                        || matches(model.teamNames.get(manager.teamId()), query);
            }
        });
        updateCount();
    }

    private static boolean matches(String name, String query) {
        return name != null && StringUtils.normalize(name).contains(query);
    }

    private void updateCount() {
        int found = table.getRowCount();
        count.setText(found == 1 ? "1 técnico encontrado" : found + " técnicos encontrados");
    }

    private static final class ManagerTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {"ID", "Técnico", "Time", "Humano", "Diretoria", "Torcida", "Troféus"};
        private static final Class<?>[] TYPES = {Integer.class, String.class, String.class, Boolean.class,
                Integer.class, Integer.class, Integer.class};
        private final Map<Integer, String> teamNames = new HashMap<>();
        private List<ManagerRow> managers = List.of();

        @Override public int getRowCount() { return managers.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int column) { return COLUMNS[column]; }
        @Override public Class<?> getColumnClass(int column) { return TYPES[column]; }

        @Override
        public Object getValueAt(int row, int column) {
            ManagerRow manager = managers.get(row);
            return switch (column) {
                case 0 -> manager.id();
                case 1 -> manager.name();
                case 2 -> teamNames.getOrDefault(manager.teamId(), "Sem time");
                case 3 -> manager.human();
                case 4 -> manager.confidenceBoard();
                case 5 -> manager.confidenceFans();
                case 6 -> manager.trophies() == null ? 0 : manager.trophies().size();
                default -> throw new IndexOutOfBoundsException(column);
            };
        }
    }
}
