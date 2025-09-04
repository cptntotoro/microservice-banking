package ru.practicum.controller.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.account.AccountCreateDto;
import ru.practicum.dto.account.AccountResponseDto;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.service.account.AccountService;

import java.util.UUID;

/**
 * Контроллер для работы со счетами
 */
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    /**
     * Сервис для работы со счетами
     */
    private final AccountService accountService;

    /**
     * Маппер счетов
     */
    private final AccountMapper accountMapper;

    /**
     * Создание нового счета
     */
    @PostMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponseDto> createAccount(
            @PathVariable UUID userId,
            @Valid @RequestBody AccountCreateDto accountDto) {
        log.info("Создание счета для пользователя: {}", userId);

        Account account = accountMapper.createDtoToAccount(accountDto);
        account.setUserId(userId);
        account.setBalance(java.math.BigDecimal.ZERO);

        return accountService.createAccount(account)
                .map(accountMapper::accountToResponseDto);
    }

    /**
     * Получение счета по идентификатору
     */
    @GetMapping("/{accountId}")
    public Mono<AccountResponseDto> getAccountById(@PathVariable UUID accountId) {
        log.info("Получение счета по ID: {}", accountId);
        return accountService.getAccountById(accountId)
                .map(accountMapper::accountToResponseDto);
    }

    /**
     * Получение всех счетов пользователя
     */
    @GetMapping("/user/{userId}")
    public Flux<AccountResponseDto> getUserAccounts(@PathVariable UUID userId) {
        log.info("Получение счетов пользователя: {}", userId);
        return accountService.getUserAccounts(userId)
                .map(accountMapper::accountToResponseDto);
    }

    /**
     * Получение счета по номеру
     */
    @GetMapping("/by-number/{accountNumber}")
    public Mono<AccountResponseDto> getAccountByNumber(@PathVariable String accountNumber) {
        log.info("Получение счета по номеру: {}", accountNumber);
        return accountService.getAccountByNumber(accountNumber)
                .map(accountMapper::accountToResponseDto);
    }

    /**
     * Удаление счета
     */
    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAccount(@PathVariable UUID accountId) {
        log.info("Удаление счета: {}", accountId);
        return accountService.deleteAccount(accountId);
    }
}