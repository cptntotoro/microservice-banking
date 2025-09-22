package ru.practicum.client.account.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для представления валюты.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CurrencyClientDto {

    /**
     * Код валюты (например, "RUB").
     */
    private String code;

    /**
     * Название валюты (например, "Российский рубль").
     */
    private String title;

    /**
     * Короткое имя валюты.
     */
    private String name;
}
