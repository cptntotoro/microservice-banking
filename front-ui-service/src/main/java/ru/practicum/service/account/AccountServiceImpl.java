package ru.practicum.service.account;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.account.dto.AccountRequestDto;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.account.Account;
import ru.practicum.model.user.UserWithAccounts;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    /**
     * Клиент для взаимодействия с сервисом аккаунтов
     */
    private final AccountServiceClient accountServiceClient;

    /**
     * Маппер для преобразования данных
     */
    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    @Override
    public Mono<UserWithAccounts> getUserWithAccounts(UUID userId) {
        log.info("Получение пользователя с счетами по ID: {}", userId);
        return accountServiceClient.getFullUser(userId)
                .map(userMapper::toUserWithAccounts)
                .doOnSuccess(userWithAccounts -> log.info("Пользователь с счетами успешно получен для ID: {}", userId))
                .doOnError(error -> log.error("Ошибка при получении пользователя с счетами для ID {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Account> createAccount(UUID userId, String currencyCode) {
        log.warn("Создание счета: {}, {}", userId, currencyCode);
        return accountServiceClient.createAccount(AccountRequestDto.builder().userId(userId).currencyCode(currencyCode).build())
                .map(accountMapper::accountResponseDtoToAccount)
                .doOnSuccess(userWithAccounts -> log.info("Счет пользователя успешно создан для ID: {}", userId))
                .doOnError(error -> log.error("Ошибка создания счета пользователя для ID {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Void> deleteAccount(UUID userId, UUID accountId) {
        log.warn("Удаление счета: {}, {}", userId, accountId);
        return accountServiceClient.deleteAccount(accountId)
                .doOnSuccess(userWithAccounts -> log.info("Счет пользователя успешно удален для ID: {}", userId))
                .doOnError(error -> log.error("Ошибка удаления счета пользователя для ID {}: {}", userId, error.getMessage()));
    }

    @Override
    public Mono<Account> getAccount(AccountRequestDto accountRequestDto) {
        return accountServiceClient.getAccount(accountRequestDto)
                .map(accountMapper::accountResponseDtoToAccount)
                .doOnSuccess(account -> log.info("Счет пользователя получен: {}", account))
                .doOnError(error -> log.error("Ошибка получения счета пользователя {}: {}", accountRequestDto, error.getMessage()));
    }

//    @Override
//    public Mono<Account> getAccountByEmail(AccountByEmailRequestDto requestDto) {
//        return accountServiceClient.getAccount(requestDto)
//                .map(accountMapper::toAccount)
//                .doOnSuccess(account -> log.info("Счет пользователя получен: {}", account))
//                .doOnError(error -> log.error("Ошибка получения счета пользователя {}: {}", requestDto, error.getMessage()));
//    }
}