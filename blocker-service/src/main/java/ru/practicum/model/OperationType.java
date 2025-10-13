package ru.practicum.model;

/**
 * Типы операций
 */
public enum OperationType {
    /**
     * Пополнение счета
     */
    DEPOSIT,

    /**
     * Снятие со счета
     */
    WITHDRAW,

    /**
     * Перевод между счетами
     */
    TRANSFER,

    /**
     * Платеж
     */
    PAYMENT
}