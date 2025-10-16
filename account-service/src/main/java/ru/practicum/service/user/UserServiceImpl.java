package ru.practicum.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
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
import ru.practicum.model.user.UserWithAccounts;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.repository.user.UserRoleRepository;
import ru.practicum.service.account.AccountService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

/**
 * Реализация сервиса для работы с пользователями
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {
    /**
     * Репозиторий пользователей
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Репозиторий ролей пользователей
     */
    @Autowired
    private UserRoleRepository userRoleRepository;

    /**
     * Сервис для работы со счетами
     */
    @Autowired
    @Lazy
    private AccountService accountService;

    /**
     * Маппер пользователей
     */
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<User> registerUser(User user, String password) {
        return validateUserAge(user.getBirthDate())
                .then(validateUniqueUsername(user.getUsername()))
                .then(validateUniqueEmail(user.getEmail()))
                .then(Mono.defer(() -> {
                    UserDao userDao = userMapper.userToUserDao(user);
                    userDao.setPasswordHash(passwordEncoder.encode(password));
                    userDao.setEnabled(true);
                    userDao.setAccountNonLocked(true);
                    userDao.setCreatedAt(LocalDateTime.now());
                    userDao.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(userDao);
                }))
                .flatMap(savedUser -> addUserRole(savedUser.getUuid(), "ROLE_USER")
                        .thenReturn(savedUser))
                .map(userMapper::userDaoToUser);
    }

    @Transactional
    @Override
    public Mono<Void> addUserRole(UUID userId, String roleName) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(user -> userRoleRepository.userHasRole(userId, roleName))
                .flatMap(hasRole -> {
                    if (hasRole) {
                        return Mono.error(new ValidationException(
                                "У пользователя уже есть роль: " + roleName,
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return userRoleRepository.addUserRole(userId, roleName);
                })
                .doOnSuccess(v -> log.info("Роль {} добавлена пользователю: {}", roleName, userId));
    }

    @Transactional
    @Override
    public Mono<Void> removeUserRole(UUID userId, String roleName) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(user -> userRoleRepository.userHasRole(userId, roleName))
                .flatMap(hasRole -> {
                    if (!hasRole) {
                        return Mono.error(new ValidationException(
                                "У пользователя нет роли: " + roleName,
                                HttpStatus.NOT_FOUND,
                                ErrorReasons.NOT_FOUND
                        ));
                    }
                    return userRoleRepository.removeUserRole(userId, roleName);
                })
                .doOnSuccess(v -> log.info("Роль {} удалена у пользователя: {}", roleName, userId));
    }

    @Override
    public Mono<Boolean> userHasRole(UUID userId, String roleName) {
        return userRoleRepository.userHasRole(userId, roleName);
    }

    @Override
    public Mono<User> getUserByUuid(UUID uuid) {
        return userRepository.findById(uuid)
                .flatMap(this::getUserWithRoles)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", uuid.toString())));
    }

    private Mono<User> getUserWithRoles(UserDao userDao) {
        return userRoleRepository.findRoleNamesByUserUuid(userDao.getUuid())
                .collectList()
                .map(roles -> {
                    User user = userMapper.userDaoToUser(userDao);
                    user.setRoles(roles);
                    return user;
                });
    }

    @Override
    public Mono<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(this::getUserWithRoles)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с username", username)));
    }

    @Override
    public Mono<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .flatMap(this::getUserWithRoles)
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь с email", email)));
    }

    @Override
    @Transactional
    public Mono<User> updateUser(UUID userId, User user) {
        return validateUserAge(user.getBirthDate())
                .then(userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new NotFoundException("Пользователь", userId.toString())))
                .flatMap(existingUserDao -> {
                    if (!existingUserDao.getUsername().equals(user.getUsername())) {
                        return validateUniqueUsername(user.getUsername()).thenReturn(existingUserDao);
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
                    updatedUserDao.setUuid(existingUserDao.getUuid());
                    updatedUserDao.setPasswordHash(existingUserDao.getPasswordHash());
                    updatedUserDao.setCreatedAt(existingUserDao.getCreatedAt());
                    updatedUserDao.setEnabled(existingUserDao.isEnabled());
                    updatedUserDao.setAccountNonLocked(existingUserDao.isAccountNonLocked());
                    return userRepository.save(updatedUserDao);
                })
                .flatMap(this::getUserWithRoles)
                .doOnSuccess(updatedUser ->
                        log.info("Данные пользователя обновлены: {}", updatedUser.getUsername()));
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
                    userDao.setAccountNonLocked(true);
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
                    userDao.setAccountNonLocked(false);
                    return userRepository.save(userDao);
                })
                .then()
                .doOnSuccess(v -> log.info("Аккаунт разблокирован: {}", userId));
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
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
        return userRepository.findByAccountNonLocked(locked)
                .map(userMapper::userDaoToUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<UserWithAccounts> getUserWithAccountsByUuid(UUID userId) {
        log.info("Получение пользователя с его счетами по ID: {}", userId);
        return getUserByUuid(userId)
                .flatMap(user -> accountService.getUserAccounts(userId)
                        .collectList()
                        .map(accounts -> UserWithAccounts.builder()
                                .user(user)
                                .accounts(accounts)
                                .build()));
    }

    public Mono<User> validateCredentials(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()))
                .map(userMapper::userDaoToUser)
                .switchIfEmpty(Mono.empty());
    }

    private Mono<Void> validateUniqueUsername(String username) {
        return userRepository.existsByUsername(username)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ValidationException(
                                "Username уже занят",
                                HttpStatus.CONFLICT,
                                ErrorReasons.DUPLICATE_ENTITY
                        ));
                    }
                    return Mono.empty();
                });
    }
}