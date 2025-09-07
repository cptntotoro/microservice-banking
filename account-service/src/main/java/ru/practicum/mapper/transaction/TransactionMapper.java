//package ru.practicum.mapper.transaction;
//
//import org.mapstruct.Mapper;
//import org.mapstruct.Mapping;
//import ru.practicum.dao.transaction.TransactionDao;
//import ru.practicum.dto.transaction.TransactionRequestDto;
//import ru.practicum.dto.transaction.TransactionResponseDto;
//import ru.practicum.model.transaction.Transaction;
//
///**
// * Маппер транзакций
// */
//@Mapper(componentModel = "spring")
//public interface TransactionMapper {
//
//    /**
//     * Смаппить DTO запроса в модель транзакции
//     */
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "currencyId", ignore = true)
//    @Mapping(target = "status", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "type", ignore = true)
//    Transaction requestDtoToTransaction(TransactionRequestDto dto);
//
//    /**
//     * Смаппить модель транзакции в DTO ответа
//     */
//    @Mapping(target = "fromAccountNumber", ignore = true)
//    @Mapping(target = "toAccountNumber", ignore = true)
//    @Mapping(target = "currencyCode", ignore = true)
//    TransactionResponseDto transactionToResponseDto(Transaction transaction);
//
//    /**
//     * Смаппить транзакцию в DAO транзакции
//     *
//     * @param transaction Транзакция
//     * @return DAO транзакции
//     */
//    TransactionDao transactionToTransactionDao(Transaction transaction);
//
//    /**
//     * Смаппить DAO транзакции в транзакцию
//     *
//     * @param transactionDao DAO транзакции
//     * @return Транзакция
//     */
//    Transaction transactionDaoToTransaction(TransactionDao transactionDao);
//}