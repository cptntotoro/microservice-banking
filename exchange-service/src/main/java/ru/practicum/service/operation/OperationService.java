package ru.practicum.service.operation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.operation.Operation;

/**
 * Сервис управления валютными операциями
 */
public interface OperationService {
    Mono<Operation> saveOperation(Operation operationDto);
    Flux<Operation> getRecentOperations(int limit);
    Flux<Operation> getOperationsByCurrencyPair(String fromCurrency, String toCurrency);
    Flux<String> getAllUsedCurrencies();
}