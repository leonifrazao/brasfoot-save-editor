package br.com.saveeditor.brasfoot;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.qt.QtDesktopApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BrasfootEditorApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BrasfootEditorApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = application.run(args);
        int exitCode = QtDesktopApplication.launch(args, context);
        SpringApplication.exit(context, () -> exitCode);
        System.exit(exitCode);
    }
}
