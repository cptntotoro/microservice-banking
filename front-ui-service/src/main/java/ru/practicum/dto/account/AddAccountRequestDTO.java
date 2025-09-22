package ru.practicum.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для запроса на добавление нового счета.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAccountRequestDTO {

    /**
     * Код валюты для нового счета.
     */
    @NotBlank(message = "Валюта обязательна")
    private String currencyCode;
}
