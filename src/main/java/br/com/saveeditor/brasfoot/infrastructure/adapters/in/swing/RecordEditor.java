package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

abstract class RecordEditor<K, R> extends JPanel {
    private final JPanel groups = new JPanel();
    private final CardLayout cards = new CardLayout();
    private final JPanel body = new JPanel(cards);
    private final JLabel title = new JLabel();
    private final JLabel pending = new JLabel();
    private final JLabel error = new JLabel();
    private final JButton apply = new JButton("Aplicar alterações");
    private final JButton discard = new JButton("Descartar alterações");
    private final JScrollPane scroll = new JScrollPane(groups);
    private final Map<JComponent, Object> baseline = new LinkedHashMap<>();
    private final Map<K, Map<JComponent, Object>> drafts = new HashMap<>();
    private boolean loading;
    private boolean applying;
    private K key;
    private R row;

    RecordEditor(String emptyText) {
        super(new BorderLayout(0, 6));
        setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 4));
        title.putClientProperty("FlatLaf.styleClass", "h3");
        pending.putClientProperty("FlatLaf.styleClass", "small");
        Color warningColor = UIManager.getColor("Component.warning.focusedBorderColor");
        if (warningColor != null) pending.setForeground(warningColor);
        error.putClientProperty("FlatLaf.styleClass", "small");
        Color errorColor = UIManager.getColor("Component.error.focusedBorderColor");
        if (errorColor != null) error.setForeground(errorColor);
        JPanel heading = new JPanel(new BorderLayout(0, 2));
        heading.add(title, BorderLayout.NORTH);
        heading.add(pending, BorderLayout.SOUTH);
        add(heading, BorderLayout.NORTH);

        groups.setLayout(new BoxLayout(groups, BoxLayout.Y_AXIS));
        groups.add(Box.createVerticalGlue());
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        body.add(scroll, "form");
        body.add(new JLabel(emptyText, SwingConstants.CENTER), "empty");
        add(body, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 6, 0));
        apply.putClientProperty("JButton.buttonType", "default");
        apply.setMargin(new Insets(4, 8, 4, 8));
        discard.setMargin(new Insets(4, 8, 4, 8));
        actions.add(apply);
        actions.add(discard);
        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.add(error, BorderLayout.NORTH);
        footer.add(actions, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        apply.addActionListener(e -> applyChanges());
        discard.addActionListener(e -> {
            drafts.remove(key);
            showRecord(key, row);
        });
        apply.setEnabled(false);
        discard.setEnabled(false);
        cards.show(body, "empty");
    }

    final JPanel group(String label) {
        JPanel section = new JPanel(new GridBagLayout()) {
            @Override public Dimension getMaximumSize() {
                return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
            }
        };
        section.setBorder(BorderFactory.createTitledBorder(label));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        groups.add(section, groups.getComponentCount() - 1);
        return section;
    }

    final <C extends JComponent> C field(JPanel section, String label, C control) {
        SwingMainWindow.addRow(section, label, control);
        baseline.put(control, null);
        if (control instanceof JComboBox<?> combo) combo.addActionListener(e -> draftChanged());
        else if (control instanceof JCheckBox check) check.addItemListener(e -> draftChanged());
        else textInput(control).getDocument().addDocumentListener(SwingMainWindow.change(this::draftChanged));
        return control;
    }

    final void showRecord(K selectedKey, R selectedRow) {
        boolean sameKey = Objects.equals(selectedKey, key);
        key = selectedKey;
        row = selectedRow;
        loading = true;
        try {
            if (selectedRow != null) {
                load(selectedRow);
                baseline.replaceAll((control, previous) -> value(control));
                Map<JComponent, Object> draft = drafts.get(key);
                if (draft != null) draft.forEach(RecordEditor::restore);
            } else {
                baseline.keySet().forEach(control -> control.setEnabled(false));
            }
        } finally {
            loading = false;
        }
        cards.show(body, row == null ? "empty" : "form");
        if (!sameKey) scroll.getViewport().setViewPosition(new Point());
        title.setText(row == null ? "Edição" : recordName(row));
        error.setText("");
        draftChanged();
    }

    final int draftCount() {
        return drafts.size();
    }

    final void clear() {
        drafts.clear();
        showRecord(null, null);
    }

    final void enableFields() {
        baseline.keySet().forEach(control -> control.setEnabled(true));
    }

    final boolean changed(JComponent field) {
        return !Objects.equals(baseline.get(field), value(field));
    }

    final String changedText(JTextField field) {
        return changed(field) ? field.getText() : null;
    }

    final Integer changedNumber(NumericSpinner field, boolean optional) {
        return changed(field) ? field.readInt(optional) : null;
    }

    final <T> Integer changedChoice(JComboBox<T> field, Function<T, Integer> code) {
        int index = field.getSelectedIndex();
        return changed(field) && index >= 0 ? code.apply(field.getItemAt(index)) : null;
    }

    private void draftChanged() {
        if (loading || applying) return;
        if (key != null) {
            Map<JComponent, Object> changes = new LinkedHashMap<>();
            baseline.forEach((control, original) -> {
                Object current = value(control);
                if (!Objects.equals(original, current)) changes.put(control, current);
            });
            if (changes.isEmpty()) drafts.remove(key);
            else drafts.put(key, changes);
        }
        boolean activeDraft = key != null && drafts.containsKey(key);
        apply.setEnabled(activeDraft);
        discard.setEnabled(activeDraft);
        String summary = drafts.size() == 1 ? "1 rascunho pendente" : drafts.size() + " rascunhos pendentes";
        pending.setText(activeDraft && drafts.size() == 1 ? "Alterações pendentes" : drafts.isEmpty() ? "" : summary);
    }

    private void applyChanges() {
        if (key == null) return;
        K editingKey = key;
        var draft = drafts.remove(editingKey);
        error.setText("");
        boolean saved = false;
        applying = true;
        try {
            saved = save(editingKey, row);
        } catch (IllegalArgumentException | ParseException e) {
            error.setText("<html>" + e.getMessage() + "</html>");
        } finally {
            applying = false;
        }
        if (saved && Objects.equals(editingKey, key)) {
            baseline.replaceAll((control, previous) -> value(control));
        } else if (!saved && draft != null) {
            drafts.put(editingKey, draft);
        }
        draftChanged();
    }

    private static JTextComponent textInput(JComponent control) {
        if (control instanceof JSpinner spinner) {
            return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        }
        return (JTextComponent) control;
    }

    private static Object value(JComponent control) {
        if (control instanceof JComboBox<?> combo) return combo.getSelectedItem();
        if (control instanceof JCheckBox check) return check.isSelected();
        return textInput(control).getText();
    }

    private static void restore(JComponent control, Object value) {
        if (control instanceof JComboBox<?> combo) combo.setSelectedItem(value);
        else if (control instanceof JCheckBox check) check.setSelected(Boolean.TRUE.equals(value));
        else textInput(control).setText((String) value);
    }

    abstract String recordName(R record);
    abstract void load(R record);
    abstract boolean save(K key, R record) throws ParseException;
}
