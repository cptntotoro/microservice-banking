package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.dao.user.UserDao;
import ru.practicum.dto.user.PasswordChangeDto;
import ru.practicum.dto.user.UserSignUpDto;
import ru.practicum.dto.user.UserResponseDto;
import ru.practicum.model.user.User;

import java.util.List;

/**
 * Маппер пользователей
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Смаппить DTO регистрации в модель пользователя
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User registrationDtoToUser(UserSignUpDto dto);

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
    @Mapping(target = "roles", ignore = true)
    User userDaoToUser(UserDao userDao);

    /**
     * Смаппить роли
     */
    @Named("mapRoles")
    default List<String> mapRoles(List<String> roles) {
        return roles != null ? roles : List.of("ROLE_USER");
    }
}
