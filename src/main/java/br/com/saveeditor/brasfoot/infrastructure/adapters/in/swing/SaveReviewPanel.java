package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.application.shared.SaveEdit;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;

final class SaveReviewPanel extends JPanel {
    SaveReviewPanel(SaveEdit.Preview preview, boolean hasDrafts) {
        super(new BorderLayout(0, 8));
        add(new JLabel("Revise as alterações antes de gravar o save."), BorderLayout.NORTH);

        JTextArea summary = new JTextArea(reviewText(preview));
        summary.setEditable(false);
        summary.setLineWrap(true);
        summary.setWrapStyleWord(true);
        summary.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(summary);
        scroll.setPreferredSize(new Dimension(630, Math.min(350, Math.max(130, 60 + summary.getLineCount() * 18))));
        add(scroll, BorderLayout.CENTER);

        String note = "Ao substituir o arquivo atual, o save antigo será guardado na pasta backups.";
        if (hasDrafts) note += "<br>Rascunhos dos formulários continuam no editor até serem aplicados.";
        add(new JLabel("<html>" + note + "</html>"), BorderLayout.SOUTH);
    }

    static String reviewText(SaveEdit.Preview preview) {
        StringBuilder output = new StringBuilder().append(preview.source()).append("\n\n");
        if (preview.steps().isEmpty()) {
            return output.append("Nenhuma alteração aplicada em relação ao arquivo atual.").toString();
        }
        for (SaveEdit.Step step : preview.steps()) {
            if (step.reversed()) output.append("Reverter no arquivo · ");
            output.append(step.edit().title()).append('\n');
            for (SaveEdit.Detail detail : step.edit().details()) {
                output.append("  ").append(detail.field()).append("  ");
                if (detail.before() == null) {
                    output.append(detail.after());
                } else {
                    output.append(step.reversed() ? detail.after() : detail.before()).append(" → ")
                            .append(step.reversed() ? detail.before() : detail.after());
                }
                output.append('\n');
            }
            output.append('\n');
        }
        return output.toString();
    }
}
