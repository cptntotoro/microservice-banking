package ru.practicum.client.cash;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.exception.ServiceUnavailableException;

import java.time.Duration;
import java.util.List;

/**
 * Клиент для сервиса обналичивания денег
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CashServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<CashResponseClientDto> deposit(CashRequestClientDto requestDto) {
        log.info("Выполняется пополнение счета: {}", requestDto);

        return getCashServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/cash/deposit";
            log.debug("Вызов сервиса наличных по адресу: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Ошибка сервиса наличных при пополнении: {}", response.statusCode());
                        return Mono.error(new ServiceUnavailableException(
                                "Ошибка пополнения счета: " + response.statusCode().value(),
                                "cash-service",
                                "Сервис обналичивания вернул ошибку: " + response.statusCode()));
                    })
                    .bodyToMono(CashResponseClientDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Пополнение успешно выполнено: {}", response))
                    .doOnError(error -> log.error("Ошибка при пополнении: {}", error.getMessage(), error));
        }).onErrorResume(e -> {
            log.error("Не удалось выполнить пополнение: {}", e.getMessage(), e);
            return Mono.error(new ServiceUnavailableException(
                    "Сервис обналичивания временно недоступен: " + e.getMessage(),
                    "cash-service",
                    "Проверьте подключение к сервису обналичивания"));
        });
    }

    public Mono<CashResponseClientDto> withdraw(CashRequestClientDto requestDto) {
        log.info("Выполняется снятие средств: {}", requestDto);

        return getCashServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/cash/withdraw";
            log.debug("Вызов сервиса наличных по адресу: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Ошибка сервиса наличных при снятии: {}", response.statusCode());
                        return Mono.error(new ServiceUnavailableException(
                                "Ошибка снятия средств: " + response.statusCode().value(),
                                "cash-service",
                                "Сервис обналичивания вернул ошибку: " + response.statusCode()));
                    })
                    .bodyToMono(CashResponseClientDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Снятие успешно выполнено: {}", response))
                    .doOnError(error -> log.error("Ошибка при снятии: {}", error.getMessage(), error));
        }).onErrorResume(e -> {
            log.error("Не удалось выполнить снятие: {}", e.getMessage(), e);
            return Mono.error(new ServiceUnavailableException(
                    "Сервис обналичивания временно недоступен: " + e.getMessage(),
                    "cash-service",
                    "Проверьте подключение к сервису обналичивания"));
        });
    }

    private Mono<String> getCashServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("cash-service");
            if (instances == null || instances.isEmpty()) {
                log.error("Экземпляры сервиса наличных не найдены");
                throw new ServiceUnavailableException(
                        "Сервис обналичивания не найден в реестре",
                        "cash-service",
                        "Не удалось обнаружить экземпляры сервиса Consul");
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Обнаружен сервис наличных по адресу: {}", url);
            return url;
        }).onErrorResume(throwable -> {
            log.error("Ошибка при обнаружении сервиса наличных: {}", throwable.getMessage(), throwable);
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса обналичивания",
                    "cash-service",
                    "Проблема с реестром сервисов: " + throwable.getMessage()));
        });
    }
}