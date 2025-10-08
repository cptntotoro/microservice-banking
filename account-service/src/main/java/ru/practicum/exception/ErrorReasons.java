package ru.practicum.exception;

/**
 * Константы для стандартизированных причин ошибок
 */
public final class ErrorReasons {

    private ErrorReasons() {}

    // Общие ошибки
    public static final String VALIDATION_ERROR = "Ошибка валидации";
    public static final String NOT_FOUND = "Ресурс не найден";
    public static final String INTERNAL_ERROR = "Внутренняя ошибка сервера";

    // Бизнес-ошибки
    public static final String INSUFFICIENT_FUNDS = "Недостаточно средств";
    public static final String DUPLICATE_ENTITY = "Дублирующаяся запись";
    public static final String AGE_RESTRICTION = "Возрастное ограничение";
    public static final String INVALID_AMOUNT = "Неверная сумма";
    public static final String INSUFFICIENT_BALANCE = "Недостаточно средств на счете";
    public static final String INVALID_OPERATION = "Неверная операция";
    public static final String CURRENCIES_NOT_INITIALIZED = "Валюты не инициализированы";

    // Ошибки аутентификации/авторизации
    public static final String UNAUTHORIZED = "Не авторизован";
    public static final String FORBIDDEN = "Доступ запрещен";

    // Дополнительные бизнес-ошибки
    public static final String ACCOUNT_EXISTS = "Счет уже существует";
    public static final String USER_EXISTS = "Пользователь уже существует";
    public static final String TRANSACTION_FAILED = "Ошибка транзакции";
    public static final String CONDITIONS_NOT_MET = "Условия не выполнены";
}