package ru.practicum.mapper.operation;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.dao.OperationDao;
import ru.practicum.dto.operation.OperationDto;
import ru.practicum.model.operation.Operation;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OperationMapper {

    /**
     * Смаппить валютную операцию в DTO валютной операции
     *
     * @param operation Валютная операция
     * @return DTO валютной операции
     */
    OperationDto operationToOperationDto(Operation operation);

    /**
     * Смаппить валютную операцию в DAO валютной операции
     *
     * @param operation Валютная операция
     * @return DAO валютной операции
     */
    OperationDao operationToOperationDao(Operation operation);

    /**
     * Смаппить DAO валютной операции в валютную операцию
     *
     * @param operationDao DAO валютной операции
     * @return Валютная операция
     */
    Operation operationDaotoOperation(OperationDao operationDao);
}