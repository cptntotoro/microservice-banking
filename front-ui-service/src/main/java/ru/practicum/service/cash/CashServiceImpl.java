package ru.practicum.service.cash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.client.cash.CashServiceClient;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.cash.CashMapper;
import ru.practicum.model.cash.Cash;
import ru.practicum.service.exchange.ExchangeRateService;

@Service
@RequiredArgsConstructor
@Slf4j
public class CashServiceImpl implements CashService {
    /**
     * Клиент для обращений к сервису обналичивания денег (cash-service)
     */
    private final CashServiceClient cashServiceClient;

    /**
     * Маппер операций с наличными
     */
    private final CashMapper cashMapper;

    /**
     * Сервис курсов обмена валют
     */
    private final ExchangeRateService exchangeRateService;

    @Override
    public Mono<Cash> deposit(Cash cash) {
        return validateCurrency(cash.getCurrency())
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Недопустимая валюта для депозита: {}", cash.getCurrency());
                        return Mono.error(new ValidationException("Недопустимая валюта: " + cash.getCurrency()));
                    }
                    log.info("Обработка депозита для счета {}: сумма {}", cash.getAccountId(), cash.getAmount());
                    return cashServiceClient.deposit(cashMapper.cashToCashRequestClientDto(cash))
                            .map(cashMapper::cashResponseClientDtoToCash);
                });
    }

    @Override
    public Mono<Cash> withdraw(Cash cash) {
        return validateCurrency(cash.getCurrency())
                .flatMap(valid -> {
                    if (!valid) {
                        log.warn("Недопустимая валюта для вывода: {}", cash.getCurrency());
                        return Mono.error(new ValidationException("Недопустимая валюта: " + cash.getCurrency()));
                    }
                    log.info("Обработка вывода для счета {}: сумма {}", cash.getAccountId(), cash.getAmount());
                    return cashServiceClient.withdraw(cashMapper.cashToCashRequestClientDto(cash))
                            .map(cashMapper::cashResponseClientDtoToCash);
                });
    }

    // Вспомогательный метод для валидации валюты
    private Mono<Boolean> validateCurrency(String currency) {
        if (currency == null) {
            return Mono.just(false);
        }
        return exchangeRateService.getAvailableCurrencies()
                .collectList()
                .map(currencies -> currencies.contains(currency))
                .defaultIfEmpty(false)
                .doOnNext(valid -> log.debug("Валидация валюты {}: {}", currency, valid));
    }

}