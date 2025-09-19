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
import ru.practicum.dto.TransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.mapper.TransferMapper;
import ru.practicum.model.TransferRequest;
import ru.practicum.service.TransferService;

/**
 * Контроллер для обработки переводов между счетами
 */
@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final TransferMapper transferMapper;

    /**
     * Перевод между собственными счетами пользователя
     *
     * @param requestDto DTO запроса на перевод
     * @return DTO ответа с информацией о переводе
     */
    @PostMapping("/own")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TransferResponseDto> transferOwn(@Valid @RequestBody TransferRequestDto requestDto) {
        TransferRequest request = transferMapper.toModel(requestDto);
        return transferService.transferBetweenOwnAccounts(request)
                .map(transferMapper::toResponseDto);
    }

    /**
     * Перевод на счет другого пользователя
     *
     * @param requestDto DTO запроса на перевод
     * @return DTO ответа с информацией о переводе
     */
    @PostMapping("/other")
    @ResponseStatus(HttpStatus.OK)
    public Mono<TransferResponseDto> transferOther(@Valid @RequestBody TransferRequestDto requestDto) {
        TransferRequest request = transferMapper.toModel(requestDto);
        return transferService.transferToOtherAccount(request)
                .map(transferMapper::toResponseDto);
    }
}