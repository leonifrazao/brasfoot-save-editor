package br.com.saveeditor.brasfoot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Brasfoot Save Editor API",
        version = "v1",
        description = "REST API for editing Brasfoot save game files in-memory.\n\n" +
                      "**Workflow:**\n" +
                      "1. **Upload save:** Use `/api/session/load` to upload a `.sav` file into memory.\n" +
                      "2. **Edit in-memory:** Use the various endpoints (Teams, Players, Managers, Tournaments, Finances) to modify the loaded state.\n" +
                      "3. **Download:** Use `/api/session/save` to download the modified `.sav` file.",
        contact = @Contact(
            name = "Brasfoot Save Editor Team",
            url = "https://github.com/your-org/brasfoot-save-editor"
        )
    ),
    servers = {
        @Server(url = "/", description = "Default Server URL")
    }
)
public class OpenApiConfig {
}
