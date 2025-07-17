package com.cozyhavenstay.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Cozy Haven Stay API",
        version = "1.0",
        description = "API documentation for the Cozy Haven Stay hotel booking application.",
        contact = @Contact(
            name = "Cozy Haven Support",
            email = "support@cozyhavenstay.com"
        ),
        license = @License(
            name = "Apache 2.0",
            url = "http://www.apache.org/licenses/LICENSE-2.0.html"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local Development Server"),
        // Add other servers like production, staging here
        // @Server(url = "https://api.cozyhavenstay.com", description = "Production Server")
    },
    security = @SecurityRequirement(name = "BearerAuth") // Apply BearerAuth globally
)
@SecurityScheme(
    name = "BearerAuth", // Name of the security scheme
    type = SecuritySchemeType.HTTP, // It's an HTTP security scheme
    bearerFormat = "JWT", // The format is JWT
    scheme = "bearer", // The scheme is "bearer"
    description = "Provide the JWT token in the format: Bearer <token>"
)
public class OpenApiConfig {
    // This class acts as configuration for OpenAPI 3 (Swagger)
}