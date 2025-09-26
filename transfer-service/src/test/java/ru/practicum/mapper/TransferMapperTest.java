package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.TransferDao;
import ru.practicum.dto.OtherTransferRequestDto;
import ru.practicum.dto.OwnTransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.model.OperationStatus;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;
import ru.practicum.model.TransferType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransferMapperTest {

    private final TransferMapper mapper = Mappers.getMapper(TransferMapper.class);

    @Test
    void transferRequestDtoToTransferRequest_shouldMapOwnTransferRequestDtoToTransferRequest() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.50");

        OwnTransferRequestDto dto = OwnTransferRequestDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        TransferRequest result = mapper.transferRequestDtoToTransferRequest(dto);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getFromAccountId());
        assertEquals(toAccountId, result.getToAccountId());
        assertEquals(amount, result.getAmount());
    }

    @Test
    void otherTransferRequestDtoToTransferRequest_shouldMapOtherTransferRequestDtoToTransferRequest() {
        UUID fromAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("500.75");

        OtherTransferRequestDto dto = OtherTransferRequestDto.builder()
                .fromAccountId(fromAccountId)
                .amount(amount)
                .build();

        TransferRequest result = mapper.otherTransferRequestDtoToTransferRequest(dto);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getFromAccountId());
        assertEquals(amount, result.getAmount());
        assertNull(result.getToAccountId());
    }

    @Test
    void transferRequestDtoToTransferRequest_shouldHandleNullDto() {
        TransferRequest result = mapper.transferRequestDtoToTransferRequest(null);

        assertNull(result);
    }

    @Test
    void otherTransferRequestDtoToTransferRequest_shouldHandleNullDto() {
        TransferRequest result = mapper.otherTransferRequestDtoToTransferRequest(null);

        assertNull(result);
    }

    @Test
    void transferResponseToTransferResponseDto_shouldMapTransferResponseToTransferResponseDto() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.50");
        BigDecimal convertedAmount = new BigDecimal("750.25");

        TransferResponse response = TransferResponse.builder()
                .status(OperationStatus.SUCCESS)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();

        TransferResponseDto result = mapper.transferResponseToTransferResponseDto(response);

        assertNotNull(result);
        assertEquals(OperationStatus.SUCCESS, result.getStatus());
        assertEquals(fromAccountId, result.getFromAccountId());
        assertEquals(toAccountId, result.getToAccountId());
        assertEquals(amount, result.getAmount());
        assertEquals(convertedAmount, result.getConvertedAmount());
    }

    @Test
    void transferResponseToTransferResponseDto_shouldHandleNullResponse() {
        TransferResponseDto result = mapper.transferResponseToTransferResponseDto(null);

        assertNull(result);
    }

    @Test
    void transferResponseToTransferResponseDto_shouldHandlePartialData() {
        TransferResponse response = TransferResponse.builder()
                .status(OperationStatus.SUCCESS)
                .amount(new BigDecimal("100.00"))
                .build();

        TransferResponseDto result = mapper.transferResponseToTransferResponseDto(response);

        assertNotNull(result);
        assertEquals(OperationStatus.SUCCESS, result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertNull(result.getFromAccountId());
        assertNull(result.getToAccountId());
        assertNull(result.getConvertedAmount());
    }

    @Test
    void transferResponseToTransferDao_shouldMapCorrectly() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.50");
        BigDecimal convertedAmount = new BigDecimal("750.25");

        TransferResponse response = TransferResponse.builder()
                .status(OperationStatus.SUCCESS)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();

        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.OWN_TRANSFER;
        String errorDescription = null;

        TransferDao result = mapper.transferResponseToTransferDao(
                response, "USD", "EUR", timestamp, type, errorDescription);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getFromAccountId());
        assertEquals(toAccountId, result.getToAccountId());
        assertEquals(amount, result.getAmount());
        assertEquals(convertedAmount, result.getConvertedAmount());
        assertEquals("USD", result.getFromCurrency());
        assertEquals("EUR", result.getToCurrency());
        assertEquals(timestamp, result.getTimestamp());
        assertEquals(OperationStatus.SUCCESS, result.getStatus());
        assertEquals(type, result.getType());
        assertEquals(errorDescription, result.getErrorDescription());
        assertNull(result.getId()); // ID генерируется базой данных
    }

    @Test
    void transferResponseToTransferDao_shouldHandleFailedStatus() {
        TransferResponse response = TransferResponse.builder()
                .status(OperationStatus.FAILED)
                .fromAccountId(UUID.randomUUID())
                .amount(new BigDecimal("500.00"))
                .build();

        LocalDateTime timestamp = LocalDateTime.now();
        TransferType type = TransferType.EXTERNAL_TRANSFER;
        String errorDescription = "Insufficient funds";

        TransferDao result = mapper.transferResponseToTransferDao(
                response, "USD", "RUB", timestamp, type, errorDescription);

        assertNotNull(result);
        assertEquals(OperationStatus.FAILED, result.getStatus());
        assertEquals(errorDescription, result.getErrorDescription());
        assertEquals(type, result.getType());
    }
}