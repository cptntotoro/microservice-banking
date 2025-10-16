package ru.practicum.mapper.account;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.account.AccountRequestDto;
import ru.practicum.dto.account.AccountResponseDto;
import ru.practicum.dto.account.AccountWithUserResponseDto;
import ru.practicum.model.account.Account;

/**
 * Маппер счетов
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * Смаппить DTO создания в модель счета
     *
     * @param accountRequestDto DTO для создания счета
     * @return Счет
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Account createDtoToAccount(AccountRequestDto accountRequestDto);

    /**
     * Смаппить модель счета в DTO ответа
     *
     * @param account Счет
     * @return DTO ответа с данными счета
     */
    AccountResponseDto accountToAccountResponseDto(Account account);

    /**
     * Смаппить счет в DTO ответа с данными счета
     *
     * @param account Счет
     * @return DTO ответа с данными счета
     */
    AccountWithUserResponseDto accountToAccountWithUserResponseDto(Account account);

    /**
     * Смаппить счет в DAO счета
     *
     * @param account Счет
     * @return DAO счета
     */
    @Mapping(target = "currencyId", ignore = true)
    AccountDao accountToAccountDao(Account account);

    /**
     * Смаппить DAO счета в счет
     *
     * @param accountDao DAO счета
     * @return Счет
     */
    @Mapping(target = "currencyCode", ignore = true)
    Account accountDaoToAccount(AccountDao accountDao);
}
