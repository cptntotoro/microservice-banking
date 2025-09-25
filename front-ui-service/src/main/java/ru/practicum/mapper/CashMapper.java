package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.cash.CashRequestClientDto;
import ru.practicum.client.cash.CashResponseClientDto;
import ru.practicum.dto.cash.CashRequestDto;
import ru.practicum.dto.cash.CashResponseDto;
import ru.practicum.model.cash.Cash;

/**
 * Маппер операций с наличными
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CashMapper {

    /**
     * Смаппить DTO запроса на операцию с наличными в модель
     *
     * @param cashRequestDto DTO запроса на операцию с наличными
     * @return Модель операции с наличными
     */
    @Mapping(target = "operationId", ignore = true)
    @Mapping(target = "operationType", ignore = true)
    @Mapping(target = "newBalance", ignore = true)
    @Mapping(target = "operationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "message", ignore = true)
    Cash cashRequestDtoToCash(CashRequestDto cashRequestDto);

    /**
     * Смаппить операцию с наличными в DTO ответа
     *
     * @param cash Модель операции с наличными
     * @return DTO ответа на операции с наличными
     */
    CashResponseDto cashToCashResponseDto(Cash cash);

    /**
     * Смаппить операцию с наличными в DTO запроса клиента
     *
     * @param cash Модель операции с наличными
     * @return DTO запроса клиента на операции с наичными
     */
    CashRequestClientDto cashToCashRequestClientDto(Cash cash);

    /**
     * Смаппить DTO ответа клиента на операцию с наличными в модель
     *
     * @param cashResponseDto DTO ответа клиента на операцию с наличными
     * @return Модель операции с наличными
     */
    Cash cashResponseClientDtoToCash(CashResponseClientDto cashResponseDto);
}