package kz.app.appstore.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop Service API")
                        .description("API для свободного пользования внешними организациями для получения различных услуг")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Shahruh Isaev")
                                .email("isaevshahruh2001@gmail.com")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth")) // ⬅️ Добавляем секьюрность
                .components(new Components()
                        .addSecuritySchemes("BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))); // ⬅️ Конфигурируем Bearer Token
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
