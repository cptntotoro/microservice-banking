package ru.practicum.mapper.operation;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.OperationDao;
import ru.practicum.dto.operation.OperationDto;
import ru.practicum.model.operation.Operation;
import ru.practicum.model.operation.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationMapperTest {

    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Test
    void operationToOperationDto() {
        Operation operation = createTestOperation();

        OperationDto result = mapper.operationToOperationDto(operation);

        assertOperationEquals(operation, result);
    }

    @Test
    void operationToOperationDao() {
        Operation operation = createTestOperation();

        OperationDao result = mapper.operationToOperationDao(operation);

        assertOperationEquals(operation, result);
    }

    @Test
    void operationDaoToOperation() {
        OperationDao dao = createTestOperationDao();

        Operation result = mapper.operationDaotoOperation(dao);

        assertOperationEquals(dao, result);
    }

    private Operation createTestOperation() {
        return Operation.builder()
                .id(UUID.randomUUID())
                .fromCurrency("USD")
                .toCurrency("RUB")
                .amount(new BigDecimal("100.00"))
                .convertedAmount(new BigDecimal("7550.00"))
                .exchangeRate(new BigDecimal("75.50"))
                .operationType(OperationType.BUY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OperationDao createTestOperationDao() {
        return OperationDao.builder()
                .id(UUID.randomUUID())
                .fromCurrency("EUR")
                .toCurrency("RUB")
                .amount(new BigDecimal("200.00"))
                .convertedAmount(new BigDecimal("17160.00"))
                .exchangeRate(new BigDecimal("85.80"))
                .operationType(OperationType.SELL)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void assertOperationEquals(Operation expected, OperationDto actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFromCurrency(), actual.getFromCurrency());
        assertEquals(expected.getToCurrency(), actual.getToCurrency());
        assertEquals(expected.getAmount(), actual.getAmount());
        assertEquals(expected.getConvertedAmount(), actual.getConvertedAmount());
        assertEquals(expected.getExchangeRate(), actual.getExchangeRate());
        assertEquals(expected.getOperationType(), actual.getOperationType());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    private void assertOperationEquals(Operation expected, OperationDao actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFromCurrency(), actual.getFromCurrency());
        assertEquals(expected.getToCurrency(), actual.getToCurrency());
        assertEquals(expected.getAmount(), actual.getAmount());
        assertEquals(expected.getConvertedAmount(), actual.getConvertedAmount());
        assertEquals(expected.getExchangeRate(), actual.getExchangeRate());
        assertEquals(expected.getOperationType(), actual.getOperationType());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }

    private void assertOperationEquals(OperationDao expected, Operation actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFromCurrency(), actual.getFromCurrency());
        assertEquals(expected.getToCurrency(), actual.getToCurrency());
        assertEquals(expected.getAmount(), actual.getAmount());
        assertEquals(expected.getConvertedAmount(), actual.getConvertedAmount());
        assertEquals(expected.getExchangeRate(), actual.getExchangeRate());
        assertEquals(expected.getOperationType(), actual.getOperationType());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }
}