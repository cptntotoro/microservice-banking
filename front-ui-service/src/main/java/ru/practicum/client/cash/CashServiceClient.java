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
 * Клиент для обращений к сервису обналичивания денег (Cash)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CashServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    /**
     * Выполняет пополнение счета
     *
     * @param requestDto DTO запроса на пополнение
     * @return Mono с ответом сервиса
     */
    public Mono<CashResponseClientDto> deposit(CashRequestClientDto requestDto) {
        log.info("Performing deposit: {}", requestDto);

        return getCashServiceUrl().flatMap(baseUrl -> {
                    String url = baseUrl + "/cash/deposit";
                    log.debug("Calling cash service at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(requestDto)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Cash service error during deposit: {}", response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка пополнения счета: " + response.statusCode().value(),
                                        "cash-service",
                                        "Сервис обналичивания вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(CashResponseClientDto.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(response -> log.info("Deposit successful: {}", response))
                            .doOnError(error -> log.error("Error during deposit: {}", error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Failed to perform deposit", e);
                    return Mono.error(new ServiceUnavailableException(
                            "Сервис обналичивания временно недоступен: " + e.getMessage(),
                            "cash-service",
                            "Проверьте подключение к сервису обналичивания"));
                });
    }

    /**
     * Выполняет снятие средств со счета
     *
     * @param requestDto DTO запроса на снятие
     * @return Mono с ответом сервиса
     */
    public Mono<CashResponseClientDto> withdraw(CashRequestClientDto requestDto) {
        log.info("Performing withdraw: {}", requestDto);

        return getCashServiceUrl().flatMap(baseUrl -> {
                    String url = baseUrl + "/cash/withdraw";
                    log.debug("Calling cash service at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(requestDto)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Cash service error during withdraw: {}", response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка снятия средств: " + response.statusCode().value(),
                                        "cash-service",
                                        "Сервис обналичивания вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(CashResponseClientDto.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(response -> log.info("Withdraw successful: {}", response))
                            .doOnError(error -> log.error("Error during withdraw: {}", error.getMessage(), error));
                })
                .onErrorResume(e -> {
                    log.error("Failed to perform withdraw", e);
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
                log.error("No instances of cash-service found");
                throw new ServiceUnavailableException(
                        "Сервис обналичивания не найден в реестре",
                        "cash-service",
                        "Не удалось обнаружить экземпляры сервиса в Eureka/Consul");
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered cash-service at: {}", url);
            return url;
        }).onErrorResume(throwable -> {
            log.error("Error discovering cash-service: {}", throwable.getMessage(), throwable);
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса обналичивания",
                    "cash-service",
                    "Проблема с реестром сервисов: " + throwable.getMessage()));
        });
    }
}