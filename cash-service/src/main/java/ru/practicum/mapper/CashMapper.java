package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.dto.CashResponseDto;
import ru.practicum.model.CashRequest;
import ru.practicum.model.CashResponse;

@Mapper(componentModel = "spring")
public interface CashMapper {
    CashRequest toModel(CashRequestDto dto);
    CashResponseDto toDto(CashResponse model);
}