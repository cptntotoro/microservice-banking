package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dto.TransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;

@Mapper(componentModel = "spring")
public interface TransferMapper {
    TransferRequest toModel(TransferRequestDto dto);
    TransferResponseDto toResponseDto(TransferResponse model);
}