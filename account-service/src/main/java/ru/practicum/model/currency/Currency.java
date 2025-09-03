package ru.practicum.model.currency;

import lombok.*;
import java.time.LocalDateTime;
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

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
