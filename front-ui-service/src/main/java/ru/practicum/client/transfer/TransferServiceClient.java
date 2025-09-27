package ru.practicum.client.transfer;

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
import java.util.UUID;

/**
 * Клиент для обращений к сервису переводов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransferServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<Void> performOwnTransfer(OwnTransferRequestClientDto dto, UUID userId) {
        log.info("Performing own transfer for user {}: amount {}", userId, dto.getAmount());

        return getTransferServiceUrl()
                .flatMap(baseUrl -> {
                    String url = baseUrl + "/api/transfer/own";
                    log.debug("Calling transfer service at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("X-User-Id", userId.toString())
                            .bodyValue(dto)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Transfer service error for user {}: {}", userId, response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка выполнения перевода: " + response.statusCode().value(),
                                        "transfer-service",
                                        "Сервис переводов вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(Void.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(v -> log.info("Own transfer successful for user {}", userId));
                })
                .onErrorResume(ServiceUnavailableException.class, this::handleServiceError)
                .onErrorResume(throwable -> {
                    log.error("Unexpected error during own transfer for user {}: {}", userId, throwable.getMessage());
                    return Mono.error(new ServiceUnavailableException(
                            "Неожиданная ошибка при выполнении перевода",
                            "transfer-service",
                            throwable.getMessage()));
                });
    }

    public Mono<Void> performOtherTransfer(OtherTransferRequestClientDto dto, UUID userId) {
        log.info("Performing other transfer for user {} to email {}: amount {}",
                userId, dto.getRecipientEmail(), dto.getAmount());

        return getTransferServiceUrl()
                .flatMap(baseUrl -> {
                    String url = baseUrl + "/api/transfer/other";
                    log.debug("Calling transfer service at: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .header("X-User-Id", userId.toString())
                            .bodyValue(dto)
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, response -> {
                                log.error("Transfer service error for user {}: {}", userId, response.statusCode());
                                return Mono.error(new ServiceUnavailableException(
                                        "Ошибка выполнения перевода: " + response.statusCode().value(),
                                        "transfer-service",
                                        "Сервис переводов вернул ошибку: " + response.statusCode()));
                            })
                            .bodyToMono(Void.class)
                            .timeout(Duration.ofSeconds(10))
                            .doOnSuccess(v -> log.info("Other transfer successful for user {}", userId));
                })
                .onErrorResume(ServiceUnavailableException.class, this::handleServiceError)
                .onErrorResume(throwable -> {
                    log.error("Unexpected error during other transfer for user {}: {}", userId, throwable.getMessage());
                    return Mono.error(new ServiceUnavailableException(
                            "Неожиданная ошибка при выполнении перевода",
                            "transfer-service",
                            throwable.getMessage()));
                });
    }

    private Mono<String> getTransferServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("transfer-service");
            if (instances.isEmpty()) {
                log.error("No instances of transfer-service found in registry");
                throw new ServiceUnavailableException(
                        "Сервис переводов не найден в реестре сервисов",
                        "transfer-service",
                        "Не удалось обнаружить экземпляры сервиса в Eureka/Consul");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered transfer-service at: {}", url);
            return url;
        }).onErrorResume(throwable -> {
            log.error("Error discovering transfer-service: {}", throwable.getMessage());
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса переводов",
                    "transfer-service",
                    "Проблема с реестром сервисов: " + throwable.getMessage()));
        });
    }

    /**
     * Обработка ошибок недоступности сервиса
     */
    private Mono<Void> handleServiceError(ServiceUnavailableException e) {
        log.error("Service unavailable error: {}", e.getMessage());
        return Mono.error(e);
    }
}