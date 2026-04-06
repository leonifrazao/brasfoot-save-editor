package br.com.saveeditor.brasfoot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Brasfoot Save Editor API",
        version = "2.0.0",
        description = "REST API for editing Brasfoot save game files in-memory."
    )
)
public class OpenApiConfig {
}
