package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.user.UserDao;
import ru.practicum.dto.user.PasswordChangeDto;
import ru.practicum.dto.user.UserRegistrationDto;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.model.user.User;

/**
 * Маппер пользователей
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Смаппить DTO регистрации в модель пользователя
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User registrationDtoToUser(UserRegistrationDto dto);

    /**
     * Смаппить модель пользователя в DTO ответа
     */
    UserResponseDto userToResponseDto(User user);

    /**
     * Смаппить DTO изменения пароля в строку пароля
     */
    default String passwordChangeDtoToString(PasswordChangeDto dto) {
        return dto != null ? dto.getNewPassword() : null;
    }

    /**
     * Смаппить пользователя в DAO пользователя
     *
     * @param user Пользователь
     * @return DAO пользователя
     */
    UserDao userToUserDao(User user);

    /**
     * Смаппить DAO пользователя в пользователя
     *
     * @param userDao DAO пользователя
     * @return Пользователь
     */
    User userDaoToUser(UserDao userDao);
}
