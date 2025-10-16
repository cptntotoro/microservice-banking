package ru.practicum.controller.account;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.account.AccountRequestDto;
import ru.practicum.dto.account.AccountResponseDto;
import ru.practicum.dto.account.AccountWithUserResponseDto;
import ru.practicum.dto.account.BalanceUpdateRequestDto;
import ru.practicum.dto.account.TransferDto;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.account.Account;
import ru.practicum.service.account.AccountService;

import java.util.UUID;

/**
 * Контроллер для работы со счетами
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Validated
public class AccountController {
    /**
     * Сервис управления счетами
     */
    private final AccountService accountService;

    /**
     * Маппер счетов
     */
    private final AccountMapper accountMapper;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto accountDto) {
        log.info("Создание счета для пользователя: {}", accountDto.getUserId());

        Account account = accountMapper.createDtoToAccount(accountDto);

        return accountService.createAccount(account)
                .map(accountMapper::accountToAccountResponseDto);
    }

    @PostMapping("/get")
    public Mono<AccountResponseDto> getAccountByCurrency(@Valid @RequestBody AccountRequestDto accountDto) {
        log.info("Получение счета по [userID, currencyCode]: {{}, {}}", accountDto.getUserId(), accountDto.getCurrencyCode());
        return accountService.findAccountByUserAndCurrency(accountDto)
                .map(accountMapper::accountToAccountResponseDto);
    }

    @GetMapping("/{accountId}")
    public Mono<AccountResponseDto> getAccountById(@PathVariable UUID accountId) {
        log.info("Получение счета по ID: {}", accountId);
        return accountService.getAccountById(accountId)
                .map(accountMapper::accountToAccountResponseDto);
    }

    @GetMapping("/user/{accountId}")
    public Mono<AccountWithUserResponseDto> getUserIdByAccountId(@PathVariable UUID accountId) {
        log.info("Получение счета по ID: {}", accountId);
        return accountService.getAccountById(accountId)
                .map(accountMapper::accountToAccountWithUserResponseDto);
    }

    @GetMapping("/user-by-email/{email}/{currency}")
    public Mono<AccountWithUserResponseDto> getAccountWithUserByEmailAndCurrency(@PathVariable String email, @PathVariable String currency) {
        log.info("Получение счета по email: {}", email);
        return accountService.getAccountByUserEmailAndCurrency(email, currency)
                .map(accountMapper::accountToAccountWithUserResponseDto);
    }

    @DeleteMapping("/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAccount(@PathVariable UUID accountId) {
        log.info("Удаление счета: {}", accountId);
        return accountService.deleteAccount(accountId);
    }

    @PostMapping("/transfer")
    public Mono<Void> transfer(@Valid @RequestBody TransferDto dto) {
        log.info("Перевод между своими счетами: с {} на {} сумма {}", dto.getFromAccountId(), dto.getToAccountId(), dto.getAmount());
        return accountService.transferBetweenAccounts(dto);
    }

    @PostMapping("/check-update-balance")
    public Mono<Boolean> checkAndUpdateBalance(@Valid @RequestBody BalanceUpdateRequestDto requestDto) {
        log.info("Обновление баланса: номер счета {} сумма {}", requestDto.getAccountId(), requestDto.getAmount());
        return accountService.checkAndUpdateBalance(requestDto);
    }

    @GetMapping("/verify/{userId}/{accountId}")
    public Mono<Boolean> verifyAccount(@PathVariable UUID userId, @PathVariable UUID accountId) {
        log.info("Верификация счета пользователя: userId={}, accountId={}", userId, accountId);
        return accountService.existsAccount(userId, accountId);
    }

}