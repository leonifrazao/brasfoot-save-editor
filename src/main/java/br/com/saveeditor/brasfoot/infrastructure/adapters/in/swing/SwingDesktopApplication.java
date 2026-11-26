package br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing;

import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import com.formdev.flatlaf.FlatLightLaf;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class SwingDesktopApplication {
    private SwingDesktopApplication() { }

    public static void launch(ConfigurableApplicationContext context) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
            } catch (Exception e) {
                System.err.println("Nao foi possivel iniciar o FlatLaf; usando o tema Swing padrao: " + e.getMessage());
            }
            SwingMainWindow window = new SwingMainWindow(context.getBean(BrasfootPresenter.class));
            window.setVisible(true);
        });
    }
}
