package ru.practicum.dto.account;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO запроса на удаление счета
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteAccountRequestDto {
    /**
     * Идентификатор счета
     */
    @NotNull(message = "Идентификатор счета не может быть пустым")
    private UUID accountId;
}
