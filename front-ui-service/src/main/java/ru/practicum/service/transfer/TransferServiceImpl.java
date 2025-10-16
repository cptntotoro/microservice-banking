package ru.practicum.service.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.transfer.TransferServiceClient;
import ru.practicum.client.transfer.dto.OtherTransferRequestClientDto;
import ru.practicum.client.transfer.dto.OwnTransferRequestClientDto;
import ru.practicum.client.transfer.dto.TransferResponseDto;
import ru.practicum.dto.transfer.OtherTransferRequestDto;
import ru.practicum.dto.transfer.OwnTransferRequestDto;
import ru.practicum.mapper.transfer.TransferMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferServiceImpl implements TransferService {
    /**
     * Клиент для обращений к сервису переводов
     */
    private final TransferServiceClient transferServiceClient;

    /**
     * Маппер переводов средств
     */
    private final TransferMapper transferMapper;

    @Override
    public Mono<TransferResponseDto> performOwnTransfer(OwnTransferRequestDto requestDto, UUID userId) {
        OwnTransferRequestClientDto clientDto = transferMapper.ownTransferRequestDtoToOwnTransferRequestClientDto(requestDto);
        clientDto.setUserId(userId);
        return transferServiceClient.performOwnTransfer(clientDto);
    }

    @Override
    public Mono<TransferResponseDto> performOtherTransfer(OtherTransferRequestDto requestDto, UUID userId) {
        OtherTransferRequestClientDto clientDto = transferMapper.otherTransferRequestDtoToOtherTransferRequestClientDto(requestDto);
        clientDto.setFromUserId(userId);
        return transferServiceClient.performOtherTransfer(clientDto);
    }
}