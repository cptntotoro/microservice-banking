package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.dto.CashResponseDto;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;

/**
 * Маппер запросов на пополнение счета
 */
@Mapper(componentModel = "spring")
public interface CashMapper {

    /**
     * Смаппить DTO запроса на пополнение счета в модель
     *
     * @param cashRequestDto DTO запроса на пополнение счета
     * @return Запрос на пополнение счета
     */
    CashRequest cashRequestDtoToCashRequest(CashRequestDto cashRequestDto);

    /**
     * Смаппить ответ на запрос на пополнение счета в DTO
     *
     * @param cashResponse Ответ на запрос на пополнение счета
     * @return DTO ответа на запрос на пополнение счета
     */
    CashResponseDto cashResponseToCashResponseDto(CashResponse cashResponse);
}