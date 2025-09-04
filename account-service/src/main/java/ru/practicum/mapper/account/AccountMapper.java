package ru.practicum.mapper.account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.account.AccountCreateDto;
import ru.practicum.dto.account.AccountResponseDto;
import ru.practicum.model.account.Account;

/**
 * Маппер счетов
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Смаппить DTO создания в модель счета
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account createDtoToAccount(AccountCreateDto dto);

    /**
     * Смаппить модель счета в DTO ответа
     */
    @Mapping(target = "currencyCode", ignore = true)
    @Mapping(target = "currencyName", ignore = true)
    AccountResponseDto accountToResponseDto(Account account);

    /**
     * Смаппить счет в DAO счета
     *
     * @param account Счет
     * @return DAO счета
     */
    AccountDao accountToAccountDao(Account account);

    /**
     * Смаппить DAO счета в счет
     *
     * @param accountDao DAO счета
     * @return Счет
     */
    Account accountDaoToAccount(AccountDao accountDao);
}
