package ru.practicum.service.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.practicum.client.exchange.ExchangeServiceClient;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.model.exchange.ExchangeRate;

/**
 * Сервис для работы с курсами валют
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeRateServiceImpl implements ExchangeRateService {
    /**
     * Клиент для сервиса обмена валют
     */
    private final ExchangeServiceClient exchangeServiceClient;

    /**
     * Маппер операций с наличными
     */
    private final ExchangeRateMapper exchangeRateMapper;

    @Override
    public Flux<ExchangeRate> getCurrentRates() {
        log.info("Получение текущих курсов валют");

        return exchangeServiceClient.getCurrentRates()
                .map(exchangeRateMapper::exchangeRateClientDtoToExchangeRate)
                .doOnNext(rate -> log.debug("Получен курс: {} -> {}",
                        rate.getBaseCurrency(), rate.getTargetCurrency()))
                .doOnComplete(() -> log.info("Все курсы успешно загружены"))
                .doOnError(error -> log.error("Ошибка при загрузке курсов: {}", error.getMessage()));
    }
}