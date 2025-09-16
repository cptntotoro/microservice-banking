package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Коды причин блокировки операций
 */
@Getter
@AllArgsConstructor
public enum BlockReasonCode {
    /**
     * Дублирующаяся операция
     */
    DUPLICATE_OPERATION("Дублирующаяся операция"),

    /**
     * Аномальная сумма операции
     */
    AMOUNT_ANOMALY("Сумма превышает среднюю в 2 раза"),

    /**
     * Операция в нерабочее время
     */
    UNUSUAL_TIME("Операция в нерабочее время (23:00-06:00)"),

    /**
     * Слишком частые операции
     */
    HIGH_FREQUENCY("Слишком частые операции (>5 за 10 минут)"),

    /**
     * Высокий совокупный риск
     */
    COMPOSITE_RISK("Высокий совокупный риск"),

    /**
     * Неизвестная причина
     */
    UNKNOWN("Неизвестная причина блокировки");

    private final String description;

}