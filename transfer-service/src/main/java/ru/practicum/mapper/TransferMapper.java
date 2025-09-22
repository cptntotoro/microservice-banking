package ru.practicum.mapper;

import org.mapstruct.Mapper;
import ru.practicum.dao.TransferDao;
import ru.practicum.dto.TransferRequestDto;
import ru.practicum.dto.TransferResponseDto;
import ru.practicum.model.TransferRequest;
import ru.practicum.model.TransferResponse;
import ru.practicum.model.TransferType;

import java.time.LocalDateTime;

/**
 * Маппер запросов на перевод
 */
@Mapper(componentModel = "spring")
public interface TransferMapper {
    /**
     * Смаппить DTO запроса на перевод в модель
     *
     * @param transferRequestDto DTO запроса на перевод
     * @return Запрос на перевод средств
     */
    TransferRequest transferRequestDtoToTransferRequest(TransferRequestDto transferRequestDto);

    /**
     * Смаппить запроса на перевод в DTO
     *
     * @param transferResponse Запрос на перевод средств
     * @return DTO запроса на перевод
     */
    TransferResponseDto transferResponseToTransferResponseDto(TransferResponse transferResponse);

    /**
     * Конвертировать TransferResponse в TransferDao для сохранения в БД
     *
     * @param response TransferResponse
     * @param fromCurrency Валюта отправителя
     * @param toCurrency Валюта получателя
     * @param timestamp Время операции
     * @param type Тип операции
     * @param errorDescription Описание ошибки (если FAILED)
     * @return TransferDao
     */
    default TransferDao toDao(TransferResponse response, String fromCurrency, String toCurrency, LocalDateTime timestamp,
                              TransferType type, String errorDescription) {
        return TransferDao.builder()
                .fromAccountId(response.getFromAccountId())
                .toAccountId(response.getToAccountId())
                .amount(response.getAmount())
                .convertedAmount(response.getConvertedAmount())
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .timestamp(timestamp)
                .status(response.getStatus())
                .type(type)
                .errorDescription(errorDescription)
                .build();
    }
}