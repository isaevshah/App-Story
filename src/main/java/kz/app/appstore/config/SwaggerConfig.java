package kz.app.appstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url("/payment-service"))
                .info(new Info()
                        .title("Online Bank Payment Service API")
                        .description("API для управления пользователем Онлайн-банкинга")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Isaev Shahruh")
                                .email("isaevshahruh2001@gmail.com")))
                // Define multiple security requirements for the API
                .components(new io.swagger.v3.oas.models.Components()
                        // Define Bearer Auth (JWT)
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi backendServiceApi() {
        return GroupedOpenApi.builder()
                .group("shop-service")
                .packagesToScan("kz.app.appstore")
                .pathsToMatch("/**")//.pathsToMatch("/")
                .build();
    }
}
