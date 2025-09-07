package ru.practicum.model;

public enum Currency {
    RUB("Российский рубль"),
    USD("Доллар США"),
    EUR("Евро"),
    CNY("Китайский юань"),
    GBP("Фунт стерлингов"),
    JPY("Японская иена");

    private final String description;

    Currency(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
