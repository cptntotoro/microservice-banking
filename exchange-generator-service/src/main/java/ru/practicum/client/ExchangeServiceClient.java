package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
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

    public Mono<Void> sendExchangeRates(List<ExchangeRateDto> rates) {
        log.info("Sending {} exchange rates to Exchange service", rates.size());

        return getExchangeServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/exchange/update-rates";
            log.debug("Calling Exchange service at: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(rates)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("Successfully sent rates to Exchange service"))
                    .doOnError(error -> log.error("Error sending rates to Exchange: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to send rates to Exchange", e);
            return Mono.error(new RuntimeException("Exchange service unavailable: " + e.getMessage()));
        });
    }

    private Mono<String> getExchangeServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("exchange-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances of exchange-service found");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered exchange-service at: {}", url);
            return url;
        });
    }
}