package com.staynest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI Configuration.
 * Access at: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .components(getComponents())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    private Info getApiInfo() {
        return new Info()
                .title("StayNest API Documentation")
                .version("1.0.0")
                .description("""
                    StayNest - Vacation Rental Platform API
                    
                    A comprehensive REST API for managing vacation rental properties, bookings, payments, and reviews.
                    
                    **Features:**
                    - User authentication with JWT
                    - Property and unit management
                    - Booking system with date conflict prevention
                    - Payment processing (V1.0: recording, V2.0: gateway integration)
                    - Review and rating system
                    - Wishlist functionality
                    - Advanced search with filters
                    - Dashboard analytics
                    
                    **Authentication:**
                    Most endpoints require JWT token authentication. Include the token in the Authorization header:
```
                    Authorization: Bearer <your-jwt-token>
```
                    
                    **Getting Started:**
                    1. Register a user: POST /api/v1/auth/register
                    2. Login: POST /api/v1/auth/login
                    3. Use the returned token for authenticated requests
                    
                    **Environments:**
                    - Development: http://localhost:8080/api/v1
                    - Production: TBD
                    """)
                .contact(new Contact()
                        .name("Sanchit Pawar")
                        .email("sanchitp.dev@gmail.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private List<Server> getServers() {
        Server devServer = new Server()
                .url("http://localhost:8080/api/v1")
                .description("Development Server");

        return List.of(devServer);
    }

    private Components getComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token (without 'Bearer' prefix)")
                );
    }
}