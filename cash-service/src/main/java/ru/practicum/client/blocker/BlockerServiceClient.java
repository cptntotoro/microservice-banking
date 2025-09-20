package ru.practicum.client.blocker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису блокировки операций
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BlockerServiceClient {

    private static final String SERVICE_NAME = "blocker-service";
    private static final String CHECK_OPERATION_ENDPOINT = "/api/blocker/check";

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    /**
     * Проверяет операцию на блокировку
     *
     * @param accountId идентификатор счета
     * @param userId идентификатор пользователя
     * @param amount сумма операции
     * @param currency валюта операции
     * @param operationType тип операции (DEPOSIT/WITHDRAWAL)
     * @return true если операция заблокирована, false если разрешена
     */
    public Mono<Boolean> checkOperation(UUID accountId, UUID userId,
                                        BigDecimal amount, String currency,
                                        String operationType) {
        log.debug("Проверка операции для счета: {}, пользователь: {}, тип: {}",
                accountId, userId, operationType);

        OperationCheckRequestDto request = createOperationCheckRequest(
                accountId, userId, amount, currency, operationType
        );

        return getBlockerServiceUrl()
                .flatMap(baseUrl -> {
                    String url = baseUrl + CHECK_OPERATION_ENDPOINT;
                    log.debug("Вызов сервиса блокировки по адресу: {}", url);

                    return webClientBuilder.build()
                            .post()
                            .uri(url)
                            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(OperationCheckResponseDto.class)
                            .map(this::processCheckResponse)
                            .doOnNext(blocked -> logOperationCheckResult(blocked, request))
                            .doOnSuccess(response -> log.info("Операция успешно проверена"))
                            .doOnError(error -> log.error("Ошибка проверки операции: {}", error.getMessage()));
                })
                .onErrorResume(this::handleServiceError);
    }

    private OperationCheckRequestDto createOperationCheckRequest(UUID accountId, UUID userId,
                                                                 BigDecimal amount, String currency,
                                                                 String operationType) {
        return OperationCheckRequestDto.builder()
                .operationId(UUID.randomUUID())
                .operationType(operationType)
                .userId(userId)
                .accountId(accountId)
                .amount(amount)
                .currency(currency)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private Boolean processCheckResponse(OperationCheckResponseDto response) {
        log.debug("Ответ проверки операции: заблокировано={}, кодПричины={}, оценкаРиска={}",
                response.isBlocked(), response.getReasonCode(), response.getRiskScore());
        return response.isBlocked();
    }

    private void logOperationCheckResult(Boolean blocked, OperationCheckRequestDto request) {
        if (Boolean.TRUE.equals(blocked)) {
            log.warn("Операция ЗАБЛОКИРОВАНА: accountId={}, userId={}, type={}, amount={} {}",
                    request.getAccountId(), request.getUserId(), request.getOperationType(),
                    request.getAmount(), request.getCurrency());
        } else {
            log.info("Операция РАЗРЕШЕНА: accountId={}, userId={}, type={}",
                    request.getAccountId(), request.getUserId(), request.getOperationType());
        }
    }

    private Mono<Boolean> handleServiceError(Throwable error) {
        log.error("Сервис блокировки недоступен. Блокируем операцию для безопасности. Ошибка: {}",
                error.getMessage());
        // В случае ошибки сервиса блокируем операцию для безопасности
        return Mono.just(true);
    }

    /**
     * Получает URL активного инстанса сервиса блокировки через Discovery Client
     */
    private Mono<String> getBlockerServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances(SERVICE_NAME);
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("Не найдены экземпляры сервиса " + SERVICE_NAME);
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();

            log.debug("Обнаружен сервис {} по адресу: {}", SERVICE_NAME, url);
            return url;
        });
    }
}