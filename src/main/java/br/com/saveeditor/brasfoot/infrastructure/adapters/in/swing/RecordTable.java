package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Point;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

final class RecordTable<R> extends JTable {
    private final Model model;
    private final TableRowSorter<Model> sorter;
    private final JScrollPane scroll;
    private final ToIntFunction<R> id;
    private Consumer<R> selectionListener = row -> {};
    private Integer lastSelectedId;
    private boolean updating;

    RecordTable(ToIntFunction<R> id, Function<R, Object[]> cells, String... columns) {
        this.id = id;
        model = new Model(cells, columns);
        setModel(model);
        sorter = new TableRowSorter<>(model);
        setRowSorter(sorter);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFillsViewportHeight(true);
        scroll = new JScrollPane(this);
        getSelectionModel().addListSelectionListener(e -> {
            if (!updating && !e.getValueIsAdjusting()) notifySelection();
        });
    }

    JScrollPane scrollPane() { return scroll; }

    void onSelection(Consumer<R> listener) { selectionListener = listener; }

    void setRows(List<R> rows, boolean newContext) {
        update(() -> {
            model.rows = rows.stream().map(record -> new Row<>(record, model.cells.apply(record))).toList();
            model.fireTableDataChanged();
        }, newContext);
    }

    void filter(Predicate<R> filter) {
        update(() -> sorter.setRowFilter(new RowFilter<>() {
            @Override public boolean include(Entry<? extends Model, ? extends Integer> entry) {
                return filter.test(model.rows.get(entry.getIdentifier()).record());
            }
        }), false);
    }

    private void update(Runnable change, boolean newContext) {
        var viewport = scroll.getViewport();
        Point position = newContext ? new Point() : viewport.getViewPosition();
        if (newContext) lastSelectedId = null;
        updating = true;
        try {
            change.run();
            clearSelection();
            int viewIndex = findVisibleSelection();
            if (viewIndex < 0 && lastSelectedId == null && getRowCount() > 0) viewIndex = 0;
            if (viewIndex >= 0) setRowSelectionInterval(viewIndex, viewIndex);
            var extent = viewport.getExtentSize();
            var size = getPreferredSize();
            viewport.setViewPosition(new Point(Math.min(position.x, Math.max(0, size.width - extent.width)),
                    Math.min(position.y, Math.max(0, size.height - extent.height))));
        } finally {
            updating = false;
        }
        notifySelection();
    }

    private int findVisibleSelection() {
        if (lastSelectedId == null) return -1;
        for (int index = 0; index < model.rows.size(); index++) {
            if (id.applyAsInt(model.rows.get(index).record()) == lastSelectedId) {
                return convertRowIndexToView(index);
            }
        }
        return -1;
    }

    private void notifySelection() {
        int viewIndex = getSelectedRow();
        R row = viewIndex < 0 ? null : model.rows.get(convertRowIndexToModel(viewIndex)).record();
        if (row != null) lastSelectedId = id.applyAsInt(row);
        selectionListener.accept(row);
    }

    private record Row<R>(R record, Object[] cells) {}

    private final class Model extends AbstractTableModel {
        private final Function<R, Object[]> cells;
        private final String[] columns;
        private List<Row<R>> rows = List.of();

        private Model(Function<R, Object[]> cells, String[] columns) {
            this.cells = cells;
            this.columns = columns;
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return columns.length; }
        @Override public String getColumnName(int column) { return columns[column]; }
        @Override public Class<?> getColumnClass(int column) {
            for (Row<R> row : rows) {
                Object value = row.cells()[column];
                if (value != null) return value.getClass();
            }
            return Object.class;
        }
        @Override public Object getValueAt(int row, int column) { return rows.get(row).cells()[column]; }
    }
}
