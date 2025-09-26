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
        UUID userId = UUID.randomUUID();
        String currencyCode = "USD";

        AccountCreateDto dto = AccountCreateDto.builder()
                .userId(userId)
                .currencyCode(currencyCode)
                .build();

        Account account = accountMapper.createDtoToAccount(dto);

        assertNotNull(account);
        assertEquals(userId, account.getUserId());
        assertEquals(currencyCode, account.getCurrencyCode());
        assertNull(account.getId());
        assertNull(account.getBalance());
        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());
    }

    @Test
    void accountToAccountResponseDto_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String currencyCode = "USD";
        BigDecimal balance = BigDecimal.valueOf(100.50);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Account account = Account.builder()
                .id(id)
                .userId(userId)
                .currencyCode(currencyCode)
                .balance(balance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountResponseDto dto = accountMapper.accountToAccountResponseDto(account);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(currencyCode, dto.getCurrencyCode());
        assertEquals(balance, dto.getBalance());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void accountToAccountDao_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String currencyCode = "USD";
        BigDecimal balance = BigDecimal.valueOf(100.50);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        Account account = Account.builder()
                .id(id)
                .userId(userId)
                .currencyCode(currencyCode)
                .balance(balance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountDao dao = accountMapper.accountToAccountDao(account);

        assertNotNull(dao);
        assertEquals(id, dao.getId());
        assertEquals(userId, dao.getUserId());
        assertEquals(balance, dao.getBalance());
        assertEquals(createdAt, dao.getCreatedAt());
        assertEquals(updatedAt, dao.getUpdatedAt());
        assertNull(dao.getCurrencyId());
    }

    @Test
    void accountDaoToAccount_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID currencyId = UUID.randomUUID();
        BigDecimal balance = BigDecimal.valueOf(100.50);
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime updatedAt = LocalDateTime.now();

        AccountDao dao = AccountDao.builder()
                .id(id)
                .userId(userId)
                .currencyId(currencyId)
                .balance(balance)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        Account account = accountMapper.accountDaoToAccount(dao);

        assertNotNull(account);
        assertEquals(id, account.getId());
        assertEquals(userId, account.getUserId());
        assertEquals(balance, account.getBalance());
        assertEquals(createdAt, account.getCreatedAt());
        assertEquals(updatedAt, account.getUpdatedAt());
        assertNull(account.getCurrencyCode());
    }
}