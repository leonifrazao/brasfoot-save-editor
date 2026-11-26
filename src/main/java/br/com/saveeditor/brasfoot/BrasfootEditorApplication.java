package br.com.saveeditor.brasfoot;

import br.com.saveeditor.brasfoot.infrastructure.adapters.in.swing.SwingDesktopApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BrasfootEditorApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(BrasfootEditorApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setHeadless(false);
        ConfigurableApplicationContext context = application.run(args);
        SwingDesktopApplication.launch(context);
    }
}
