package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.currencies")
@Getter
@Setter
public class CurrencyConfig {
    /**
     * Поддерживаемые валюты (3-буквенные коды)
     */
    private List<String> supported;
}