package ru.practicum.mapper.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.account.AccountDao;
import ru.practicum.dto.account.AccountCreateDto;
import ru.practicum.dto.account.AccountResponseDto;
import ru.practicum.model.account.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = Mappers.getMapper(AccountMapper.class);
    }

    @Test
    void createDtoToAccount_shouldMapCorrectly() {
        UUID currencyId = UUID.randomUUID();
        String accountNumber = "1234567890";
        AccountCreateDto dto = AccountCreateDto.builder()
                .currencyId(currencyId)
                .accountNumber(accountNumber)
                .build();

        Account account = accountMapper.createDtoToAccount(dto);

        assertNotNull(account);
        assertEquals(currencyId, account.getCurrencyCode());
        assertEquals(accountNumber, account.getAccountNumber());
        assertNull(account.getId());
        assertNull(account.getUserId());
        assertNull(account.getBalance());
        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());
    }

    @Test
    void accountToResponseDto_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100.50);
        String accountNumber = "1234567890";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Account account = Account.builder()
                .id(id)
                .userId(userId)
                .currencyCode(currencyId)
                .balance(balance)
                .accountNumber(accountNumber)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountResponseDto dto = accountMapper.accountToResponseDto(account);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(currencyId, dto.getCurrencyId());
        assertEquals(balance, dto.getBalance());
        assertEquals(accountNumber, dto.getAccountNumber());
        assertEquals(createdAt, dto.getCreatedAt());
        assertNull(dto.getCurrencyCode());
        assertNull(dto.getCurrencyName());
    }

    @Test
    void accountToAccountDao_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100.50);
        String accountNumber = "1234567890";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Account account = Account.builder()
                .id(id)
                .userId(userId)
                .currencyCode(currencyId)
                .balance(balance)
                .accountNumber(accountNumber)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountDao dao = accountMapper.accountToAccountDao(account);

        assertNotNull(dao);
        assertEquals(id, dao.getId());
        assertEquals(userId, dao.getUserId());
        assertEquals(currencyId, dao.getCurrencyId());
        assertEquals(balance, dao.getBalance());
        assertEquals(accountNumber, dao.getAccountNumber());
        assertEquals(createdAt, dao.getCreatedAt());
        assertEquals(updatedAt, dao.getUpdatedAt());
    }

    @Test
    void accountDaoToAccount_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100.50);
        String accountNumber = "1234567890";
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AccountDao dao = AccountDao.builder()
                .id(id)
                .userId(userId)
                .currencyId(currencyId)
                .balance(balance)
                .accountNumber(accountNumber)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Account account = accountMapper.accountDaoToAccount(dao);

        assertNotNull(account);
        assertEquals(id, account.getId());
        assertEquals(userId, account.getUserId());
        assertEquals(currencyId, account.getCurrencyCode());
        assertEquals(balance, account.getBalance());
        assertEquals(accountNumber, account.getAccountNumber());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(updatedAt, account.getUpdatedAt());
    }
}