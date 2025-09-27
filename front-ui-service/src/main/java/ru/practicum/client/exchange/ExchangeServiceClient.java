package ru.practicum.client.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Клиент для сервиса обмена валют
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    /**
     * Получение текущих курсов валют от сервиса обмена
     */
    public Flux<ExchangeRateClientDto> getCurrentRates() {
        log.info("Запрос текущих курсов валют от сервиса обмена");

        return getExchangeServiceUrl().flatMapMany(baseUrl -> {
            String url = baseUrl + "/api/exchange/rates";
            log.debug("Вызов сервиса обмена по адресу: {}", url);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(ExchangeRateClientDto.class)
                    .doOnNext(rate -> log.debug("Получен курс: {} -> {}",
                            rate.getBaseCurrency(), rate.getTargetCurrency()))
                    .doOnComplete(() -> log.info("Все курсы успешно получены от сервиса обмена"))
                    .doOnError(error -> log.error("Ошибка при получении курсов от сервиса обмена: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Не удалось получить курсы от сервиса обмена", e);
            return Flux.error(new RuntimeException("Сервис обмена недоступен: " + e.getMessage()));
        });
    }

    /**
     * Получение URL сервиса обмена через Discovery Client
     */
    private Mono<String> getExchangeServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("exchange-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("Не найдено экземпляров сервиса обмена");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Обнаружен сервис обмена по адресу: {}", url);
            return url;
        });
    }
}