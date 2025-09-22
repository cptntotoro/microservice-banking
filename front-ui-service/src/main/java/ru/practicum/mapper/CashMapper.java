package ru.practicum.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;
import ru.practicum.dto.cash.CashRequestDto;
import ru.practicum.dto.cash.CashResponseDto;
import ru.practicum.model.cash.Cash;

import java.util.List;

/**
 * Маппер для операций с наличными с использованием MapStruct.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface CashMapper {

    /**
     * Маппинг DTO запроса в модель.
     *
     * @param dto DTO запроса
     * @return модель для внутренней обработки
     */
    @Mapping(target = "operationId", ignore = true)
    @Mapping(target = "operationType", ignore = true)
    @Mapping(target = "newBalance", ignore = true)
    @Mapping(target = "operationDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "message", ignore = true)
    Cash toModel(CashRequestDto dto);

    /**
     * Маппинг модели в DTO ответа.
     *
     * @param model внутренняя модель
     * @return DTO ответа
     */
    CashResponseDto toDto(Cash model);

    /**
     * Маппинг списка моделей в список DTO ответов.
     *
     * @param models список внутренних моделей
     * @return список DTO ответов
     */
    List<CashResponseDto> toDtoList(List<Cash> models);

    /**
     * Обновление существующего DTO на основе модели.
     *
     * @param entity модель
     * @param update DTO для обновления
     */
    void updateDtoFromModel(Cash entity, @MappingTarget CashResponseDto update);

    /**
     * Создание DTO ошибки на основе исключения.
     *
     * @param dto целевой DTO
     * @param exception исключение
     */
    @AfterMapping
    default void createErrorDto(@MappingTarget CashResponseDto dto, Exception exception) {
        dto.setStatus("ERROR");
        dto.setMessage(exception.getMessage() != null ? exception.getMessage() : "Произошла ошибка");
    }
}