package ru.practicum.controller.cash;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.cash.CashRequestDto;
import ru.practicum.dto.cash.CashResponseDto;
import ru.practicum.mapper.CashMapper;
import ru.practicum.service.cash.CashService;

/**
 * Контроллер для операций с наличными
 */
@RestController
@RequestMapping("/cash")
@RequiredArgsConstructor
@Slf4j
public class CashController {

    /**
     * Сервис пополнения счета
     */
    private final CashService cashService;

    /**
     * MapStruct маппер для операций с наличными
     */
    private final CashMapper cashMapper;

    /**
     * Пополнение счета
     *
     * @param requestDto валидированный DTO запроса на пополнение
     * @return Mono с ResponseEntity и результатом операции
     */
    @PostMapping("/deposit")
    public Mono<ResponseEntity<CashResponseDto>> deposit(@Valid @RequestBody CashRequestDto requestDto) {
        log.info("Processing deposit request: accountId={}, amount={}",
                requestDto.getAccountId(), requestDto.getAmount());

        return cashService.deposit(cashMapper.toModel(requestDto))
                .map(cashMapper::toDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Deposit completed successfully: {}", response.getBody()))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Invalid deposit request: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .body(cashMapper.createErrorDto(new CashResponseDto(), e)));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error during deposit", e);
                    return Mono.just(ResponseEntity.status(500)
                            .body(cashMapper.createErrorDto(new CashResponseDto(), e)));
                });
    }

    /**
     * Снятие средств со счета
     *
     * @param requestDto валидированный DTO запроса на снятие
     * @return Mono с ResponseEntity и результатом операции
     */
    @PostMapping("/withdraw")
    public Mono<ResponseEntity<CashResponseDto>> withdraw(@Valid @RequestBody CashRequestDto requestDto) {
        log.info("Processing withdraw request: accountId={}, amount={}",
                requestDto.getAccountId(), requestDto.getAmount());

        return cashService.withdraw(cashMapper.toModel(requestDto))
                .map(cashMapper::toDto)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Withdraw completed successfully: {}", response.getBody()))
                .onErrorResume(IllegalArgumentException.class, e -> {
                    log.warn("Invalid withdraw request: {}", e.getMessage());
                    return Mono.just(ResponseEntity.badRequest()
                            .body(cashMapper.createErrorDto(new CashResponseDto(), e)));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Unexpected error during withdraw", e);
                    return Mono.just(ResponseEntity.status(500)
                            .body(cashMapper.createErrorDto(new CashResponseDto(), e)));
                });
    }
}