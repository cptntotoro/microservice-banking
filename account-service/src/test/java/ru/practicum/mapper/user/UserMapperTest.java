package ru.practicum.mapper.user;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.user.UserDao;
import ru.practicum.dto.user.PasswordChangeDto;
import ru.practicum.dto.user.UserRegistrationDto;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.model.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void registrationDtoToUser_shouldMapCorrectly() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setFirstName("John");
        dto.setLastName("Doe");

        User user = userMapper.registrationDtoToUser(dto);

        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getPasswordHash());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
    }

    @Test
    void registrationDtoToUser_shouldHandleNullDto() {
        User user = userMapper.registrationDtoToUser(null);

        assertNull(user);
    }

    @Test
    void userToResponseDto_shouldMapCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());

        UserResponseDto dto = userMapper.userToResponseDto(user);

        assertNotNull(dto);
        assertEquals(USER_ID, dto.getId());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals(user.getCreatedAt(), dto.getCreatedAt());
    }

    @Test
    void userToResponseDto_shouldHandleNullUser() {
        UserResponseDto dto = userMapper.userToResponseDto(null);

        assertNull(dto);
    }

    @Test
    void passwordChangeDtoToString_shouldReturnNewPassword() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setNewPassword("newPassword123");

        String result = userMapper.passwordChangeDtoToString(dto);

        assertEquals("newPassword123", result);
    }

    @Test
    void passwordChangeDtoToString_shouldHandleNullDto() {
        String result = userMapper.passwordChangeDtoToString(null);

        assertNull(result);
    }

    @Test
    void passwordChangeDtoToString_shouldHandleNullNewPassword() {
        PasswordChangeDto dto = new PasswordChangeDto();
        dto.setNewPassword(null);

        String result = userMapper.passwordChangeDtoToString(dto);

        assertNull(result);
    }

    @Test
    void userToUserDao_shouldMapCorrectly() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("hashedPassword");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        user.setCreatedAt(createdAt);
        user.setUpdatedAt(updatedAt);

        UserDao userDao = userMapper.userToUserDao(user);

        assertNotNull(userDao);
        assertEquals(USER_ID, userDao.getId());
        assertEquals("test@example.com", userDao.getEmail());
        assertEquals("John", userDao.getFirstName());
        assertEquals("Doe", userDao.getLastName());
        assertEquals("hashedPassword", userDao.getPasswordHash());
        assertEquals(createdAt, userDao.getCreatedAt());
        assertEquals(updatedAt, userDao.getUpdatedAt());
    }

    @Test
    void userToUserDao_shouldHandleNullUser() {
        UserDao userDao = userMapper.userToUserDao(null);

        assertNull(userDao);
    }

    @Test
    void userDaoToUser_shouldMapCorrectly() {
        UserDao userDao = new UserDao();
        userDao.setId(USER_ID);
        userDao.setEmail("test@example.com");
        userDao.setFirstName("John");
        userDao.setLastName("Doe");
        userDao.setPasswordHash("hashedPassword");
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();
        userDao.setCreatedAt(createdAt);
        userDao.setUpdatedAt(updatedAt);

        User user = userMapper.userDaoToUser(userDao);

        assertNotNull(user);
        assertEquals(USER_ID, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("hashedPassword", user.getPasswordHash());
        assertEquals(createdAt, user.getCreatedAt());
        assertEquals(updatedAt, user.getUpdatedAt());
    }

    @Test
    void userDaoToUser_shouldHandleNullUserDao() {
        User user = userMapper.userDaoToUser(null);

        assertNull(user);
    }
}