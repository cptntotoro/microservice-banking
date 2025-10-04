package ru.practicum.client.notification;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.notification.dto.NotificationRequestDto;

/**
 * Клиент для обращений к сервису оповещений
 */
@Component
@Slf4j
public class NotificationsServiceClient extends BaseServiceClient {

    @Autowired
    public NotificationsServiceClient(@Qualifier("notificationServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "notification-service";
    }

    public Mono<Void> sendNotification(NotificationRequestDto request) {
        String path = "/notifications/send";
        String operation = "Send notification: " + request;
        String errorPrefix = "Ошибка отправки нотификации: ";
        return performMono(HttpMethod.POST, path, request, Void.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Notification sent"));
    }
}