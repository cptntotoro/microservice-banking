package ru.practicum.service.operation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.operation.Operation;

/**
 * Сервис управления валютными операциями
 */
public interface OperationService {

    /**
     * Сохранить операцию
     *
     * @param operation Валютная операция
     * @return Валютная операция
     */
    Mono<Operation> saveOperation(Operation operation);

    /**
     * Получить недавние операции
     *
     * @param limit Количество
     * @return Список валютных операций
     */
    Flux<Operation> getRecentOperations(int limit);

    /**
     * Поллучить все операции по валютной паре
     *
     * @param fromCurrency Исходная валюта
     * @param toCurrency Целевая валюта
     * @return Список валютных операций
     */
    Flux<Operation> getOperationsByCurrencyPair(String fromCurrency, String toCurrency);

    /**
     * Получить все используемые валюты
     *
     * @return Список валютных кодов
     */
    Flux<String> getAllUsedCurrencies();
}