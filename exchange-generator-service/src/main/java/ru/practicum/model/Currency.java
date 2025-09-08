package ru.practicum.model;

import lombok.Getter;

/**
 * Перечисление валют с поддержкой расширения
 */
@Getter
public enum Currency {
    RUB("RUB", "Russian Ruble"),
    USD("USD", "US Dollar"),
    EUR("EUR", "Euro"),
    CNY("CNY", "Chinese Yuan"),
    GBP("GBP", "British Pound"),
    JPY("JPY", "Japanese Yen");

    private final String code;
    private final String name;

    Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Получить валюту по коду
     */
    public static Currency fromCode(String code) {
        for (Currency currency : values()) {
            if (currency.getCode().equalsIgnoreCase(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }

    /**
     * Проверить существование валюты по коду
     */
    public static boolean isValidCurrency(String code) {
        for (Currency currency : values()) {
            if (currency.getCode().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Получить все коды валют
     */
    public static String[] getAllCodes() {
        Currency[] currencies = values();
        String[] codes = new String[currencies.length];
        for (int i = 0; i < currencies.length; i++) {
            codes[i] = currencies[i].getCode();
        }
        return codes;
    }

    /**
     * Получить все валюты кроме базовой (RUB)
     */
    public static Currency[] getAllExceptBase() {
        Currency[] all = values();
        Currency[] result = new Currency[all.length - 1];
        int index = 0;
        for (Currency currency : all) {
            if (!currency.equals(RUB)) {
                result[index++] = currency;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return code;
    }
}