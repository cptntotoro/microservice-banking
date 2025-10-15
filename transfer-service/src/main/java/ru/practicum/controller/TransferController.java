package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.OtherTransferRequestDto;
import ru.practicum.dto.OwnTransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.mapper.TransferMapper;
import ru.practicum.model.TransferRequest;
import ru.practicum.service.TransferService;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {
    /**
     * Сервис перевода средств между счетами
     */
    private final TransferService transferService;

    /**
     * Маппер запросов на перевод
     */
    private final TransferMapper transferMapper;

    @PostMapping("/own")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TransferResponseDto> transferOwn(@Valid @RequestBody OwnTransferRequestDto requestDto) {
        return transferService.transferBetweenOwnAccounts(requestDto)
                .map(transferMapper::transferResponseToTransferResponseDto);
    }

    @PostMapping("/other")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TransferResponseDto> transferOther(@Valid @RequestBody OtherTransferRequestDto requestDto) {
        return transferService.transferToOtherAccount(requestDto)
                .map(transferMapper::transferResponseToTransferResponseDto);
    }
}