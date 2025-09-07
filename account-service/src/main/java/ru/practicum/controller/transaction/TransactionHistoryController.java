//package ru.practicum.controller.transaction;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import ru.practicum.dto.transaction.TransactionResponseDto;
//import ru.practicum.mapper.transaction.TransactionMapper;
//import ru.practicum.service.transaction.TransactionService;
//
//import java.util.UUID;
//
///**
// * Контроллер для работы с историей транзакций
// */
//@Slf4j
//@RestController
//@RequestMapping("/transactions/history")
//@RequiredArgsConstructor
//public class TransactionHistoryController {
//    /**
//     * Сервис для работы с транзакциями
//     */
//    private final TransactionService transactionService;
//
//    /**
//     * Маппер транзакций
//     */
//    private final TransactionMapper transactionMapper;
//
//    /**
//     * Получение истории транзакций по счету
//     */
//    @GetMapping("/account/{accountId}")
//    public Flux<TransactionResponseDto> getAccountTransactions(@PathVariable UUID accountId) {
//        log.info("Получение истории транзакций по счету: {}", accountId);
//        return transactionService.getAccountTransactions(accountId)
//                .map(transactionMapper::transactionToResponseDto);
//    }
//
//    /**
//     * Получение истории транзакций по пользователю
//     */
//    @GetMapping("/user/{userId}")
//    public Flux<TransactionResponseDto> getUserTransactions(@PathVariable UUID userId) {
//        log.info("Получение истории транзакций по пользователю: {}", userId);
//        return transactionService.getUserTransactions(userId)
//                .map(transactionMapper::transactionToResponseDto);
//    }
//
//    /**
//     * Получение транзакции по идентификатору
//     */
//    @GetMapping("/{transactionId}")
//    public Mono<TransactionResponseDto> getTransactionById(@PathVariable UUID transactionId) {
//        log.info("Получение транзакции по ID: {}", transactionId);
//        return transactionService.getTransactionById(transactionId)
//                .map(transactionMapper::transactionToResponseDto);
//    }
//}