package ru.practicum.mapper.cash;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.cash.dto.CashRequestClientDto;
import ru.practicum.client.cash.dto.CashResponseClientDto;
import ru.practicum.dto.cash.CashRequestDto;
import ru.practicum.dto.cash.CashResponseDto;
import ru.practicum.dto.cash.DepositWithdrawCashRequestDto;
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
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "message", ignore = true)
    Cash cashRequestDtoToCash(CashRequestDto cashRequestDto);

    @Mapping(target = "operationId", ignore = true)
    @Mapping(target = "operationType", source = "operation")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "message", ignore = true)
    Cash cashRequestDtoToCash(DepositWithdrawCashRequestDto cashRequestDto);

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
    @Mapping(target = "operationId", ignore = true)
    @Mapping(target = "accountId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "operationType", ignore = true)
    @Mapping(target = "amount", ignore = true)
    @Mapping(target = "currency", ignore = true)
    Cash cashResponseClientDtoToCash(CashResponseClientDto cashResponseDto);
}