package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.TransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransferMapperTest {

    private final TransferMapper mapper = Mappers.getMapper(TransferMapper.class);

    @Test
    void toModel_shouldMapTransferRequestDtoToTransferRequest() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        String toAccountNumber = "40702810500000012345";
        BigDecimal amount = new BigDecimal("1000.50");

        TransferRequestDto dto = TransferRequestDto.builder()
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .toAccountNumber(toAccountNumber)
                .amount(amount)
                .build();

        TransferRequest result = mapper.transferRequestDtoToTransferRequest(dto);

        assertNotNull(result);
        assertEquals(fromAccountId, result.getFromAccountId());
        assertEquals(toAccountId, result.getToAccountId());
        assertEquals(toAccountNumber, result.getToAccountNumber());
        assertEquals(amount, result.getAmount());
    }

    @Test
    void transferRequestDtoToTransferRequest_shouldHandleNullDto() {
        TransferRequest result = mapper.transferRequestDtoToTransferRequest(null);

        assertNull(result);
    }

    @Test
    void toResponseDto_shouldMapTransferResponseTransferResponseToTransferTransferResponseDto() {
        UUID fromAccountId = UUID.randomUUID();
        UUID toAccountId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("1000.50");
        BigDecimal convertedAmount = new BigDecimal("750.25");

        TransferResponse response = TransferResponse.builder()
                .status("SUCCESS")
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .convertedAmount(convertedAmount)
                .build();

        TransferResponseDto result = mapper.transferResponseToTransferResponseDto(response);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
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
                .status("SUCCESS")
                .amount(new BigDecimal("100.00"))
                .build();

        TransferResponseDto result = mapper.transferResponseToTransferResponseDto(response);

        assertNotNull(result);
        assertEquals("SUCCESS", result.getStatus());
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertNull(result.getFromAccountId());
        assertNull(result.getToAccountId());
        assertNull(result.getConvertedAmount());
    }
}