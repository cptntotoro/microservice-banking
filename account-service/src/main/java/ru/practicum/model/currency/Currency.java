package ru.practicum.model.currency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Валюта
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    /**
     * Идентификатор
     */
    private UUID id;

    /**
     * Код валюты
     */
    private String code;

    /**
     * Название валюты
     */
    private String name;
}
