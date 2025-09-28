package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.account.account.AccountServiceClient;
import ru.practicum.mapper.user.UserMapper;
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

    @Override
    public Mono<UserWithAccounts> getUserWithAccounts(UUID userId) {
        log.info("Получение пользователя с счетами по ID: {}", userId);
        return accountServiceClient.getFullUser(userId)
                .map(userMapper::toUserWithAccounts)
                .doOnSuccess(userWithAccounts -> log.info("Пользователь с счетами успешно получен для ID: {}", userId))
                .doOnError(error -> log.error("Ошибка при получении пользователя с счетами для ID {}: {}", userId, error.getMessage()));
    }
}