package ru.practicum.mapper.cash;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.cash.dto.CashResponseClientDto;
import ru.practicum.dto.cash.CashResponseDto;
import ru.practicum.model.cash.Cash;

/**
 * Маппер операций с наличными
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CashMapper {
    /**
     * Смаппить операцию с наличными в DTO ответа
     *
     * @param cash Модель операции с наличными
     * @return DTO ответа на операции с наличными
     */
    CashResponseDto cashToCashResponseDto(Cash cash);

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