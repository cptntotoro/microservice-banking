package ru.practicum.model.currency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Валюта
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Currency {
    /**
     * Код валюты
     */
    private String code;

    /**
     * Название валюты
     */
    private String name;
}