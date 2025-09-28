package ru.practicum.mapper.account;

import org.mapstruct.Mapper;
import ru.practicum.client.account.account.AccountResponseClientDto;
import ru.practicum.dto.account.AccountDashboardDto;
import ru.practicum.model.account.Account;

/**
 * Маппер для преобразования моделей счета в DTO
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Преобразование Account в AccountDashboardDto
     */
    AccountDashboardDto toAccountDashboardDto(Account account);

    /**
     * Преобразование AccountResponseClientDto в Account
     */
    Account toAccount(AccountResponseClientDto dto);
}