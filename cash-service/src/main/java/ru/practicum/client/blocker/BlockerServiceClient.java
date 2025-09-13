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
import ru.practicum.model.CashRequest;

import java.util.List;

/**
 * Клиент обращения к сервису блокировки подозрительных операций
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BlockerServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<Boolean> isOperationBlocked(CashRequest request) {
        log.info("Checking if operation is blocked for account {}", request.getAccountId());

        return getBlockerServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/blocker/check";
            log.debug("Calling Blocker service at: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .doOnSuccess(response -> log.info("Successfully checked operation"))
                    .doOnError(error -> log.error("Error checking operation: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to check operation, assuming blocked", e);
            return Mono.just(true); // Block operation if service is unavailable
        });
    }

    private Mono<String> getBlockerServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("blocker-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances of blocker-service found");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered blocker-service at: {}", url);
            return url;
        });
    }
}