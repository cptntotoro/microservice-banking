package ru.practicum.client.blocker;

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
import ru.practicum.exception.ServiceClientException;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockerServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<OperationCheckResponseDto> checkOperation(OperationCheckRequestDto request) {
        return getBlockerServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/blocker/check";
            log.debug("Проверка на подозрительность: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "blocker-service",
                                            "checkOperation",
                                            "Ошибка при проверке безопасности: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(OperationCheckResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("blocker-service", "checkOperation", "Таймаут при проверке безопасности"))
                    .doOnError(error -> log.error("Ошибка при проверке безопасности: {}", error.getMessage()));
        });
    }

    private Mono<String> getBlockerServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("blocker-service");
            if (instances == null || instances.isEmpty()) {
                throw ServiceClientException.unavailable("blocker-service", "discovery", "Сервис безопасности недоступен");
            }
            ServiceInstance instance = instances.getFirst();
            return "http://" + instance.getHost() + ":" + instance.getPort();
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к blocker-service: {}", e.getMessage());
            return Mono.error(ServiceClientException.unavailable("blocker-service", "discovery", e.getMessage()));
        });
    }
}