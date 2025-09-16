package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.OperationCheckRequestDto;
import ru.practicum.dto.OperationCheckResponseDto;
import ru.practicum.model.BlockReasonCode;
import ru.practicum.model.OperationCheckRequest;
import ru.practicum.model.OperationCheckResponse;
import ru.practicum.model.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationMapperTest {

    private final OperationMapper mapper = Mappers.getMapper(OperationMapper.class);

    @Test
    void operationCheckRequestDtoToOperationCheckRequest_shouldMapCorrectly() {
        UUID operationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        OperationCheckRequestDto dto = OperationCheckRequestDto.builder()
                .operationId(operationId)
                .operationType("DEPOSIT")
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("1000.50"))
                .currency("RUB")
                .timestamp(timestamp)
                .build();

        OperationCheckRequest result = mapper.operationCheckRequestDtoToOperationCheckRequest(dto);

        assertNotNull(result);
        assertEquals(operationId, result.getOperationId());
        assertEquals(OperationType.DEPOSIT, result.getOperationType());
        assertEquals(userId, result.getUserId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(new BigDecimal("1000.50"), result.getAmount());
        assertEquals("RUB", result.getCurrency());
        assertEquals(timestamp, result.getTimestamp());
    }

    @Test
    void operationCheckRequestDtoToOperationCheckRequest_shouldHandleAllOperationTypes() {
        OperationCheckRequestDto depositDto = createRequestDto("DEPOSIT");
        OperationCheckRequestDto withdrawalDto = createRequestDto("WITHDRAWAL");
        OperationCheckRequestDto transferDto = createRequestDto("TRANSFER");
        OperationCheckRequestDto paymentDto = createRequestDto("PAYMENT");

        assertEquals(OperationType.DEPOSIT, mapper.operationCheckRequestDtoToOperationCheckRequest(depositDto).getOperationType());
        assertEquals(OperationType.WITHDRAWAL, mapper.operationCheckRequestDtoToOperationCheckRequest(withdrawalDto).getOperationType());
        assertEquals(OperationType.TRANSFER, mapper.operationCheckRequestDtoToOperationCheckRequest(transferDto).getOperationType());
        assertEquals(OperationType.PAYMENT, mapper.operationCheckRequestDtoToOperationCheckRequest(paymentDto).getOperationType());
    }

    @Test
    void operationCheckResponseToOperationCheckResponseDto_shouldMapCorrectly() {
        OperationCheckResponse response = OperationCheckResponse.builder()
                .blocked(true)
                .reasonCode(BlockReasonCode.AMOUNT_ANOMALY)
                .description("Amount anomaly detected")
                .riskScore(80)
                .build();

        OperationCheckResponseDto result = mapper.operationCheckResponseToOperationCheckResponseDto(response);

        assertNotNull(result);
        assertTrue(result.isBlocked());
        assertEquals("AMOUNT_ANOMALY", result.getReasonCode());
        assertEquals("Amount anomaly detected", result.getDescription());
        assertEquals(80, result.getRiskScore());
    }

    @Test
    void operationCheckResponseToOperationCheckResponseDto_shouldHandleNullReasonCode() {
        OperationCheckResponse response = OperationCheckResponse.builder()
                .blocked(false)
                .reasonCode(null)
                .description(null)
                .riskScore(10)
                .build();

        OperationCheckResponseDto result = mapper.operationCheckResponseToOperationCheckResponseDto(response);

        assertNotNull(result);
        assertFalse(result.isBlocked());
        assertNull(result.getReasonCode());
        assertNull(result.getDescription());
        assertEquals(10, result.getRiskScore());
    }

    @Test
    void operationCheckRequestToOperationCheckRequestDto_shouldMapCorrectly() {
        UUID operationId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.now();

        OperationCheckRequest request = OperationCheckRequest.builder()
                .operationId(operationId)
                .operationType(OperationType.DEPOSIT)
                .userId(userId)
                .accountId(accountId)
                .amount(new BigDecimal("2000.75"))
                .currency("USD")
                .timestamp(timestamp)
                .build();

        OperationCheckRequestDto result = mapper.operationCheckRequestToOperationCheckRequestDto(request);

        assertNotNull(result);
        assertEquals(operationId, result.getOperationId());
        assertEquals("DEPOSIT", result.getOperationType());
        assertEquals(userId, result.getUserId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(new BigDecimal("2000.75"), result.getAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(timestamp, result.getTimestamp());
    }

    private OperationCheckRequestDto createRequestDto(String operationType) {
        return OperationCheckRequestDto.builder()
                .operationId(UUID.randomUUID())
                .operationType(operationType)
                .userId(UUID.randomUUID())
                .accountId(UUID.randomUUID())
                .amount(new BigDecimal("1000.00"))
                .currency("RUB")
                .timestamp(LocalDateTime.now())
                .build();
    }
}