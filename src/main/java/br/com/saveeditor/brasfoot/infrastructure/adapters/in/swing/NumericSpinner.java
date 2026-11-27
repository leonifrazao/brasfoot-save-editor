package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.text.ParseException;

final class NumericSpinner extends JSpinner {
    private final long minimum;
    private final long maximum;
    private final long initial;
    private final String label;

    NumericSpinner(String label, long initial, long minimum, long maximum) {
        super(new SpinnerNumberModel(Long.valueOf(initial), Long.valueOf(minimum), Long.valueOf(maximum), Long.valueOf(1)));
        this.label = label;
        this.initial = initial;
        this.minimum = minimum;
        this.maximum = maximum;
        var editor = new NumberEditor(this, "0");
        setEditor(editor);
        var formatter = new NumberFormatter() {
            @Override public Object stringToValue(String text) throws ParseException {
                Long value = parse(text, true);
                return value == null ? NumericSpinner.this.initial : value;
            }

            @Override public String valueToString(Object value) {
                return value == null ? "" : value.toString();
            }
        };
        editor.getTextField().setFormatterFactory(new DefaultFormatterFactory(formatter));
        editor.getTextField().setColumns(10);
        editor.getTextField().setFocusLostBehavior(JFormattedTextField.PERSIST);
    }

    JFormattedTextField input() {
        return ((NumberEditor) getEditor()).getTextField();
    }

    void display(Number value) {
        setValue(value == null ? initial : value.longValue());
        input().setText(value == null ? "" : value.toString());
    }

    Long read(boolean optional) {
        try {
            return parse(input().getText(), optional);
        } catch (ParseException e) {
            input().requestFocusInWindow();
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    Integer readInt(boolean optional) {
        Long value = read(optional);
        return value == null ? null : Math.toIntExact(value);
    }

    private Long parse(String text, boolean optional) throws ParseException {
        if (optional && text.isBlank()) return null;
        try {
            long value = Long.parseLong(text.strip());
            if (value >= minimum && value <= maximum) return value;
        } catch (NumberFormatException ignored) {
            // The same message covers out-of-range and malformed values.
        }
        throw new ParseException(label + ": informe um número inteiro entre " + minimum + " e " + maximum + ".", 0);
    }
}
