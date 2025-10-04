package ru.practicum.mapper.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import ru.practicum.client.transfer.dto.OtherTransferRequestClientDto;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.model.transfer.OtherTransfer;
import ru.practicum.model.transfer.OwnTransfer;

/**
 * Маппер переводов средств
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface TransferMapper {
    /**
     * Смаппить перевод средств на свой счет в DTO
     *
     * @param ownTransfer Перевод средств между своими счетами
     * @return DTO запроса на перевод средств между своими счетами
     */
    OwnTransferRequestDto ownTransferToOwnTransferRequestDto(OwnTransfer ownTransfer);

    /**
     * Смаппить перевод средств другому пользователю в DTO
     *
     * @param otherTransfer Перевод средств другому пользователю
     * @return DTO запроса на перевод средств другому пользователю
     */
    @Mapping(target = "fromUserId", source = "userId")
    OtherTransferRequestDto otherTransferToOtherTransferRequestDto(OtherTransfer otherTransfer);

    /**
     * Смаппить DTO запроса на перевод средств между своими счетами в клиентский DTO
     *
     * @param ownTransferRequestDto DTO запроса на перевод средств между своими счетами
     * @return Клиентский DTO запроса на перевод средств между своими счетами
     */
    OwnTransferRequestClientDto ownTransferRequestDtoToOwnTransferRequestClientDto(OwnTransferRequestDto ownTransferRequestDto);

    /**
     * Смаппить DTO запроса на перевод средств другому пользователю в клиентский DTO
     *
     * @param otherTransferRequestDto DTO запроса на перевод средств другому пользователю
     * @return Клиентский DTO запроса на перевод средств другому пользователю
     */
    OtherTransferRequestClientDto otherTransferRequestDtoToOtherTransferRequestClientDto(OtherTransferRequestDto otherTransferRequestDto);
}