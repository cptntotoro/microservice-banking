package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;
import ru.practicum.exception.ErrorReasons;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.account.AccountService;

import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;

/**
 * Реализация сервиса для работы с пользователями
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Сервис для работы со счетами
     */
    private final AccountService accountService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<User> registerUser(User user, String password) {
        return validateUserAge(user.getBirthDate())
                .then(validateUniqueLogin(user.getLogin()))
                .then(validateUniqueEmail(user.getEmail()))
                .then(Mono.defer(() -> {
                    UserDao userDao = userMapper.userToUserDao(user);
                    userDao.setPasswordHash(passwordEncoder.encode(password));
                    userDao.setEnabled(true);
                    userDao.setAccountLocked(false);
                    return userRepository.save(userDao);
                }))
                .map(userMapper::userDaoToUser)
                .doOnSuccess(registeredUser -> log.info("Пользователь зарегистрирован: {}", registeredUser.getLogin()));
    }

    @Override
    public Mono<User> getUserById(UUID userId) {
        return userRepository.findById(userId)
                .map(userMapper::userDaoToUser)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())));
    }

    @Override
    public Mono<User> getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .map(userMapper::userDaoToUser)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с логином", login)));
    }

    @Override
    @Transactional
    public Mono<User> updateUser(UUID userId, User user) {
        return validateUserAge(user.getBirthDate())
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(existingUserDao -> {
                    if (!existingUserDao.getLogin().equals(user.getLogin())) {
                        return validateUniqueLogin(user.getLogin()).thenReturn(existingUserDao);
                    }
                    return Mono.just(existingUserDao);
                })
                .flatMap(existingUserDao -> {
                    if (!existingUserDao.getEmail().equals(user.getEmail())) {
                        return validateUniqueEmail(user.getEmail()).thenReturn(existingUserDao);
                    }
                    return Mono.just(existingUserDao);
                })
                .flatMap(existingUserDao -> {
                    UserDao updatedUserDao = userMapper.userToUserDao(user);
                    updatedUserDao.setId(existingUserDao.getId());
                    updatedUserDao.setPasswordHash(existingUserDao.getPasswordHash());
                    updatedUserDao.setCreatedAt(existingUserDao.getCreatedAt());
                    updatedUserDao.setEnabled(existingUserDao.isEnabled());
                    updatedUserDao.setAccountLocked(existingUserDao.isAccountLocked());
                    return userRepository.save(updatedUserDao);
                })
                .map(userMapper::userDaoToUser)
                .doOnSuccess(updatedUser -> log.info("Данные пользователя обновлены: {}", updatedUser.getLogin()));
    }

    @Override
    @Transactional
    public Mono<Void> changePassword(UUID userId, String newPassword) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(userDao -> {
                    userDao.setPasswordHash(passwordEncoder.encode(newPassword));
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Пароль пользователя изменен: {}", userId));
    }

    @Override
    @Transactional
    public Mono<Void> deleteUser(UUID userId) {
        return accountService.getUserAccounts(userId)
                .collectList()
                .flatMap(accounts -> {
                    boolean hasBalance = accounts.stream()
                            .anyMatch(account -> account.getBalance().compareTo(java.math.BigDecimal.ZERO) > 0);
                    if (hasBalance) {
                        return Mono.error(new ValidationException(
                                "Невозможно удалить пользователя с ненулевыми счетами",
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return userRepository.deleteById(userId);
                })
                .doOnSuccess(v -> log.info("Пользователь удален: {}", userId));
    }

    @Transactional
    @Override
    public Mono<Void> activateUser(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(userDao -> {
                    userDao.setEnabled(true);
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Пользователь активирован: {}", userId));
    }

    @Transactional
    @Override
    public Mono<Void> deactivateUser(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(userDao -> {
                    userDao.setEnabled(false);
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Пользователь деактивирован: {}", userId));
    }

    @Transactional
    @Override
    public Mono<Void> lockAccount(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(userDao -> {
                    userDao.setAccountLocked(true);
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Аккаунт заблокирован: {}", userId));
    }

    @Transactional
    @Override
    public Mono<Void> unlockAccount(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(userDao -> {
                    userDao.setAccountLocked(false);
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Аккаунт разблокирован: {}", userId));
    }

    @Override
    public Mono<Boolean> existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private Mono<Void> validateUserAge(LocalDate birthDate) {
        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age < 18) {
            return Mono.error(new ValidationException(
                    "Пользователь должен быть старше 18 лет",
                    HttpStatus.BAD_REQUEST,
                    ErrorReasons.AGE_RESTRICTION
            ));
        }
        return Mono.empty();
    }

    private Mono<Void> validateUniqueLogin(String login) {
        return userRepository.existsByLogin(login)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ValidationException(
                                "Логин уже занят",
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateUniqueEmail(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ValidationException(
                                "Email уже занят",
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Flux<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return userRepository.findAll()
                .map(userMapper::userDaoToUser);
    }

    @Override
    public Flux<User> getUsersByStatus(boolean enabled) {
        log.info("Получение пользователей по статусу активности: {}", enabled);
        return userRepository.findByEnabled(enabled)
                .map(userMapper::userDaoToUser);
    }

    @Override
    public Flux<User> getUsersByLockStatus(boolean locked) {
        log.info("Получение пользователей по статусу блокировки: {}", locked);
        return userRepository.findByAccountLocked(locked)
                .map(userMapper::userDaoToUser);
    }
}