package ru.practicum.client.notification;

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
 * Клиент обращения к сервису оповещений
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<Void> sendNotification(NotificationRequestDto request) {
        log.info("Sending notification to user {}", request.getUserId());

        return getNotificationsServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/notifications/send";
            log.debug("Calling Notifications service at: {}", url);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(response -> log.info("Successfully sent notification"))
                    .doOnError(error -> log.error("Error sending notification: {}", error.getMessage()));
        }).onErrorResume(e -> {
            log.error("Failed to send notification", e);
            return Mono.empty();
        });
    }

    private Mono<String> getNotificationsServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("notifications-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("No instances of notifications-service found");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Discovered notifications-service at: {}", url);
            return url;
        });
    }
}