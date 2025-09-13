package ru.practicum.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.dto.CashResponseDto;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CashMapperTest {

    private final CashMapper mapper = Mappers.getMapper(CashMapper.class);

    @Test
    void testToModelCashRequestDto() {
        CashRequestDto dto = CashRequestDto.builder()
                .accountId("acc123")
                .userId("user456")
                .amount(1000.0)
                .currency("RUB")
                .build();

        CashRequest model = mapper.toModel(dto);

        assertNotNull(model);
        assertEquals(dto.getAccountId(), model.getAccountId());
        assertEquals(dto.getUserId(), model.getUserId());
        assertEquals(dto.getAmount(), model.getAmount());
        assertEquals(dto.getCurrency(), model.getCurrency());
    }

    @Test
    void testToDtoCashResponse() {
        CashResponse model = CashResponse.builder()
                .status("SUCCESS")
                .message("Deposit successful")
                .build();

        CashResponseDto dto = mapper.toDto(model);

        assertNotNull(dto);
        assertEquals(model.getStatus(), dto.getStatus());
        assertEquals(model.getMessage(), dto.getMessage());
    }
}