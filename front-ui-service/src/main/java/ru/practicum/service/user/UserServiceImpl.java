package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.AccountServiceClient;
import ru.practicum.client.account.dto.AddAccountRequestDto;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.account.Account;
import ru.practicum.model.user.UserWithAccounts;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

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
        return accountServiceClient.createAccount(AddAccountRequestDto.builder().userId(userId).currencyCode(currencyCode).build())
                .map(accountMapper::toAccount)
                .doOnSuccess(userWithAccounts -> log.info("Счет пользователя успешно создан для ID: {}", userId))
                .doOnError(error -> log.error("Ошибка создания счета пользователя для ID {}: {}", userId, error.getMessage()));
    }
}