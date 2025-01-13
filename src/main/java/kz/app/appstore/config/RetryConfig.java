package kz.app.appstore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class RetryConfig {
    // Этот класс может быть пустым.
    // Аннотация @EnableRetry активирует функциональность повторных попыток.
}
