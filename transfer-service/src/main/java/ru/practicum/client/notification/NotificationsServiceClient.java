package ru.practicum.client.notification;

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

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису оповещений
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationsServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<Void> sendNotification(UUID userId, String message) {
        NotificationRequestDto request = new NotificationRequestDto(userId, message);
        return getNotificationsServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/notifications/notify";
            log.debug("Отправка уведомления: {}", url);
            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Ошибка отправки уведомления")))
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10));
        });
    }

    private Mono<String> getNotificationsServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("notifications-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("Экземпляры notifications-service не найдены");
            }
            ServiceInstance instance = instances.getFirst();
            return "http://" + instance.getHost() + ":" + instance.getPort();
        });
    }
}