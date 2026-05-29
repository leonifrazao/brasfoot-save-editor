package br.com.saveeditor.brasfoot.infrastructure.adapters.in.qt;

import br.com.saveeditor.brasfoot.presentation.presenter.BrasfootPresenter;
import io.qt.widgets.QApplication;
import org.springframework.context.ApplicationContext;

public final class QtDesktopApplication {

    private QtDesktopApplication() {
    }

    public static int launch(String[] args, ApplicationContext context) {
        QApplication.initialize(args);
        QtMainWindow window = new QtMainWindow(context.getBean(BrasfootPresenter.class));
        window.show();
        int exitCode = QApplication.exec();
        QApplication.shutdown();
        return exitCode;
    }
}
