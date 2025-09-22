package ru.practicum.mapper.currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.currency.CurrencyDao;
import ru.practicum.model.currency.Currency;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CurrencyMapperTest {

    private CurrencyMapper currencyMapper;

    @BeforeEach
    void setUp() {
        currencyMapper = Mappers.getMapper(CurrencyMapper.class);
    }

    @Test
    void currencyToCurrencyDao_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        String code = "USD";
        String name = "US Dollar";

        Currency currency = Currency.builder()
                .id(id)
                .code(code)
                .name(name)
                .build();

        CurrencyDao dao = currencyMapper.currencyToCurrencyDao(currency);

        assertNotNull(dao);
        assertEquals(id, dao.getId());
        assertEquals(code, dao.getCode());
        assertEquals(name, dao.getName());
    }

    @Test
    void currencyDaoToCurrency_shouldMapCorrectly() {
        UUID id = UUID.randomUUID();
        String code = "USD";
        String name = "US Dollar";

        CurrencyDao dao = CurrencyDao.builder()
                .id(id)
                .code(code)
                .name(name)
                .build();

        Currency currency = currencyMapper.currencyDaoToCurrency(dao);

        assertNotNull(currency);
        assertEquals(id, currency.getId());
        assertEquals(code, currency.getCode());
        assertEquals(name, currency.getName());
    }
}