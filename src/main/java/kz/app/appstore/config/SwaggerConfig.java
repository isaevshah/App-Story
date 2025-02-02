package kz.app.appstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
//                .addServersItem(new Server().url("/shop-service"))
                .info(new Info()
                        .title("Shop Service API")
                        .description("API для свободного пользования внешними организациями для получения различных услуг")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Shahruh Isaev")
                                .email("shahruhi@bankffin.kz")));
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
