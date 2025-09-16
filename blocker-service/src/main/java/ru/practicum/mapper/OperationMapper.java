package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.OperationCheckRequestDto;
import ru.practicum.dto.OperationCheckResponseDto;
import ru.practicum.dto.OperationHistoryResponseDto;
import ru.practicum.model.OperationCheckRequest;
import ru.practicum.model.OperationCheckResponse;
import ru.practicum.model.OperationHistory;

/**
 * Маппер операций
 */
@Mapper(componentModel = "spring")
public interface OperationMapper {

    /**
     * Смаппить запрос на проверку операции в DTO
     *
     * @param operationCheckRequest Запрос на проверку операции
     * @return DTO запроса на проверку операции
     */
    OperationCheckRequestDto operationCheckRequestToOperationCheckRequestDto(OperationCheckRequest operationCheckRequest);

    /**
     * Смаппить DTO запроса на проверку операции в модель
     *
     * @param operationCheckRequestDto DTO запроса на проверку операции
     * @return Запрос на проверку операции
     */
    OperationCheckRequest operationCheckRequestDtoToOperationCheckRequest(OperationCheckRequestDto operationCheckRequestDto);

    /**
     * Смаппить результат проверки операции в DTO
     *
     * @param operationCheckResponse Результат проверки операции
     * @return DTO результата проверки операции
     */
    OperationCheckResponseDto operationCheckResponseToOperationCheckResponseDto(OperationCheckResponse operationCheckResponse);

    /**
     * Смаппить модель истории операций в DTO ответа
     *
     * @param operationHistory Модель истории операций
     * @return DTO ответа с историей операций
     */
    OperationHistoryResponseDto operationHistoryToOperationHistoryResponseDto(OperationHistory operationHistory);
}
