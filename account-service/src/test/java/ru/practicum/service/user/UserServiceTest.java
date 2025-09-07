package ru.practicum.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.user.UserDao;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private final UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final LocalDate birthDate = LocalDate.of(1990, 1, 1);

    private User createTestUser() {
        return User.builder()
                .uuid(userId)
                .username("testuser")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .birthDate(birthDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(true)
                .accountNonLocked(false)
                .build();
    }

    private UserDao createTestUserDao() {
        return UserDao.builder()
                .uuid(userId)
                .username("testuser")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .birthDate(birthDate)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .enabled(true)
                .accountNonLocked(false)
                .build();
    }

//    @Test
//    void registerUser_ValidUser_ReturnsRegisteredUser() {
//        User user = createTestUser();
//        UserDao userDao = createTestUserDao();
//        String password = "password123";
//
//        when(userRepository.existsByUsername(user.getUsername())).thenReturn(Mono.just(false));
//        when(userRepository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));
//        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
//        when(userMapper.userToUserDao(user)).thenReturn(userDao);
//        when(userRepository.save(any(UserDao.class))).thenReturn(Mono.just(userDao));
//        when(userMapper.userDaoToUser(userDao)).thenReturn(user);
//
//        StepVerifier.create(userService.registerUser(user, password))
//                .expectNext(user)
//                .verifyComplete();
//
//        verify(userRepository).existsByUsername(user.getUsername());
//        verify(userRepository).existsByEmail(user.getEmail());
//        verify(passwordEncoder).encode(password);
//        verify(userRepository).save(any(UserDao.class));
//    }

    @Test
    void registerUser_UserUnder18_ThrowsValidationException() {
        User user = createTestUser();
        user.setBirthDate(LocalDate.now().minusYears(17));

        when(userRepository.existsByUsername(any())).thenReturn(Mono.just(false));
        when(userRepository.existsByEmail(any())).thenReturn(Mono.just(false));

        StepVerifier.create(userService.registerUser(user, "password123"))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void registerUser_LoginAlreadyExists_ThrowsValidationException() {
        User user = createTestUser();
        String password = "password123";

        when(userRepository.existsByUsername(user.getUsername())).thenReturn(Mono.just(true));
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(Mono.just(false));

        StepVerifier.create(userService.registerUser(user, password))
                .expectError(ValidationException.class)
                .verify();

        verify(userRepository).existsByUsername(user.getUsername());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any());
    }

//    @Test
//    void getUserById_UserExists_ReturnsUser() {
//        UserDao userDao = createTestUserDao();
//        User expectedUser = createTestUser();
//
//        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
//        when(userMapper.userDaoToUser(userDao)).thenReturn(expectedUser);
//
//        StepVerifier.create(userService.getUserByUuid(userId))
//                .expectNext(expectedUser)
//                .verifyComplete();
//
//        verify(userRepository).findById(userId);
//    }

    @Test
    void getUserById_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.getUserByUuid(userId))
                .expectError(NotFoundException.class)
                .verify();

        verify(userRepository).findById(userId);
    }

//    @Test
//    void getUserByLogin_UserExists_ReturnsUser() {
//        String login = "testuser";
//        UserDao userDao = createTestUserDao();
//        User expectedUser = createTestUser();
//
//        when(userRepository.findByUsername(login)).thenReturn(Mono.just(userDao));
//        when(userMapper.userDaoToUser(userDao)).thenReturn(expectedUser);
//
//        StepVerifier.create(userService.getUserByUsername(login))
//                .expectNext(expectedUser)
//                .verifyComplete();
//
//        verify(userRepository).findByUsername(login);
//    }

//    @Test
//    void updateUser_ValidUpdate_ReturnsUpdatedUser() {
//        User updatedUser = createTestUser();
//        updatedUser.setFirstName("Jane");
//        updatedUser.setLastName("Smith");
//
//        UserDao existingUserDao = createTestUserDao();
//        UserDao updatedUserDao = createTestUserDao();
//        updatedUserDao.setFirstName("Jane");
//        updatedUserDao.setLastName("Smith");
//
//        when(userRepository.findById(userId)).thenReturn(Mono.just(existingUserDao));
//        when(userMapper.userToUserDao(updatedUser)).thenReturn(updatedUserDao);
//        when(userRepository.save(any(UserDao.class))).thenReturn(Mono.just(updatedUserDao));
//        when(userMapper.userDaoToUser(updatedUserDao)).thenReturn(updatedUser);
//
//        StepVerifier.create(userService.updateUser(userId, updatedUser))
//                .expectNext(updatedUser)
//                .verifyComplete();
//
//        verify(userRepository).findById(userId);
//        verify(userRepository, never()).existsByUsername(any());
//        verify(userRepository, never()).existsByEmail(any());
//        verify(userRepository).save(any(UserDao.class));
//    }

    @Test
    void changePassword_UserExists_PasswordChanged() {
        UserDao userDao = createTestUserDao();
        String newPassword = "newPassword123";

        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
        when(passwordEncoder.encode(newPassword)).thenReturn("newEncodedPassword");
        when(userRepository.save(userDao)).thenReturn(Mono.just(userDao));

        StepVerifier.create(userService.changePassword(userId, newPassword))
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(userDao);
    }

    @Test
    void activateUser_UserExists_ActivatesUser() {
        UserDao userDao = createTestUserDao();
        userDao.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
        when(userRepository.save(userDao)).thenReturn(Mono.just(userDao));

        StepVerifier.create(userService.activateUser(userId))
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(userRepository).save(userDao);
        assert userDao.isEnabled();
    }

    @Test
    void deactivateUser_UserExists_DeactivatesUser() {
        UserDao userDao = createTestUserDao();
        userDao.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
        when(userRepository.save(userDao)).thenReturn(Mono.just(userDao));

        StepVerifier.create(userService.deactivateUser(userId))
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(userRepository).save(userDao);
        assert !userDao.isEnabled();
    }

    @Test
    void lockAccount_UserExists_LocksAccount() {
        UserDao userDao = createTestUserDao();
        userDao.setAccountNonLocked(false);

        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
        when(userRepository.save(userDao)).thenReturn(Mono.just(userDao));

        StepVerifier.create(userService.lockAccount(userId))
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(userRepository).save(userDao);
        assert userDao.isAccountNonLocked();
    }

    @Test
    void unlockAccount_UserExists_UnlocksAccount() {
        UserDao userDao = createTestUserDao();
        userDao.setAccountNonLocked(true);

        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
        when(userRepository.save(userDao)).thenReturn(Mono.just(userDao));

        StepVerifier.create(userService.unlockAccount(userId))
                .verifyComplete();

        verify(userRepository).findById(userId);
        verify(userRepository).save(userDao);
        assert !userDao.isAccountNonLocked();
    }

//    @Test
//    void deleteUser_UserWithZeroBalance_DeletesUser() {
//        // Arrange
//        UserDao userDao = createTestUserDao();
//
//        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
//        when(accountService.getUserAccounts(userId)).thenReturn(Mono.empty());
//        when(userRepository.deleteById(userId)).thenReturn(Mono.empty());
//
//        // Act & Assert
//        StepVerifier.create(userService.deleteUser(userId))
//                .verifyComplete();
//
//        verify(accountService).getUserAccounts(userId);
//        verify(userRepository).deleteById(userId);
//    }
//
//    @Test
//    void deleteUser_UserWithNonZeroBalance_ThrowsValidationException() {
//        // Arrange
//        UserDao userDao = createTestUserDao();
//
//        when(userRepository.findById(userId)).thenReturn(Mono.just(userDao));
//        // Симулируем наличие счетов с ненулевым балансом
//        when(accountService.getUserAccounts(userId)).thenReturn(Mono.just(
//                new Account() {{
//                    setBalance(java.math.BigDecimal.TEN);
//                }}
//        ));
//
//        // Act & Assert
//        StepVerifier.create(userService.deleteUser(userId))
//                .expectErrorMatches(throwable -> throwable instanceof ValidationException &&
//                        ((ValidationException) throwable).getStatus() == HttpStatus.CONFLICT)
//                .verify();
//
//        verify(accountService).getUserAccounts(userId);
//        verify(userRepository, never()).deleteById(any());
//    }

    @Test
    void updateUser_UserNotFound_ThrowsNotFoundException() {
        User updatedUser = createTestUser();
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.updateUser(userId, updatedUser))
                .expectError(NotFoundException.class)
                .verify();

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_LoginChangedToExisting_ThrowsValidationException() {
        User updatedUser = createTestUser();
        updatedUser.setUsername("existinglogin");

        UserDao existingUserDao = createTestUserDao();

        when(userRepository.findById(userId)).thenReturn(Mono.just(existingUserDao));
        when(userRepository.existsByUsername("existinglogin")).thenReturn(Mono.just(true));

        StepVerifier.create(userService.updateUser(userId, updatedUser))
                .expectError(ValidationException.class)
                .verify();

        verify(userRepository).findById(userId);
        verify(userRepository).existsByUsername("existinglogin");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_UserNotFound_ThrowsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.changePassword(userId, "newPassword"))
                .expectError(NotFoundException.class)
                .verify();

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).encode(any());
    }
}
