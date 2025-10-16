package ru.practicum.mapper.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.transfer.dto.OtherTransferRequestClientDto;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;

/**
 * Маппер переводов средств
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TransferMapper {

    /**
     * Смаппить DTO запроса на перевод средств между своими счетами в клиентский DTO
     *
     * @param ownTransferRequestDto DTO запроса на перевод средств между своими счетами
     * @return Клиентский DTO запроса на перевод средств между своими счетами
     */
    @Mapping(target = "userId", ignore = true)
    OwnTransferRequestClientDto ownTransferRequestDtoToOwnTransferRequestClientDto(OwnTransferRequestDto ownTransferRequestDto);

    /**
     * Смаппить DTO запроса на перевод средств другому пользователю в клиентский DTO
     *
     * @param otherTransferRequestDto DTO запроса на перевод средств другому пользователю
     * @return Клиентский DTO запроса на перевод средств другому пользователю
     */
    @Mapping(target = "fromUserId", ignore = true)
    OtherTransferRequestClientDto otherTransferRequestDtoToOtherTransferRequestClientDto(OtherTransferRequestDto otherTransferRequestDto);
}