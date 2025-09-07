//package ru.practicum.controller.transaction;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.*;
//import reactor.core.publisher.Mono;
//import ru.practicum.dto.transaction.TransactionRequestDto;
//import ru.practicum.mapper.transaction.TransactionMapper;
//import ru.practicum.service.transaction.TransactionService;
//
//import java.util.UUID;
//
///**
// * Контроллер для работы с транзакциями
// */
//@Slf4j
//@RestController
//@RequestMapping("/transactions")
//@RequiredArgsConstructor
//public class TransactionController {
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
//     * Перевод денег между счетами
//     */
//    @PostMapping("/transfer")
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<Void> transferMoney(@Valid @RequestBody TransactionRequestDto transactionDto) {
//        log.info("Перевод денег между счетами: {} -> {}",
//                transactionDto.getFromAccountId(), transactionDto.getToAccountId());
//
//        ru.practicum.model.transaction.Transaction transaction =
//                transactionMapper.requestDtoToTransaction(transactionDto);
//        return transactionService.transferMoney(transaction);
//    }
//
//    /**
//     * Пополнение счета
//     */
//    @PostMapping("/{accountId}/deposit")
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<Void> depositMoney(
//            @PathVariable UUID accountId,
//            @RequestParam Double amount,
//            @RequestParam(required = false) String description) {
//        log.info("Пополнение счета: {}, сумма: {}", accountId, amount);
//        return transactionService.depositMoney(accountId, amount, description);
//    }
//
//    /**
//     * Снятие денег со счета
//     */
//    @PostMapping("/{accountId}/withdraw")
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<Void> withdrawMoney(
//            @PathVariable UUID accountId,
//            @RequestParam Double amount,
//            @RequestParam(required = false) String description) {
//        log.info("Снятие денег со счета: {}, сумма: {}", accountId, amount);
//        return transactionService.withdrawMoney(accountId, amount, description);
//    }
//}