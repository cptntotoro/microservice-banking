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

    @Override
    public String toString() {
        return code;
    }
}