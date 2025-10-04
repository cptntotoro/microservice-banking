package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.client.account.dto.UserFullResponseClientDto;
import ru.practicum.dto.user.UserDashboardDto;
import ru.practicum.mapper.account.AccountMapper;
import ru.practicum.model.user.UserWithAccounts;

/**
 * Маппер для преобразования моделей пользователя в DTO
 */
@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface UserMapper {

    /**
     * Преобразование UserWithAccounts в UserDashboardDto
     */
    @Mapping(target = "uuid", source = "user.uuid")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "birthDate", source = "user.birthDate")
    @Mapping(target = "accounts", source = "accounts")
    UserDashboardDto toUserDashboardDto(UserWithAccounts userWithAccounts);

    /**
     * Преобразование UserFullResponseClientDto в UserWithAccounts
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "accounts", source = "accounts")
    UserWithAccounts toUserWithAccounts(UserFullResponseClientDto dto);
}