package ru.practicum.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import ru.practicum.dto.NotificationRequestDto;

@Component
@RequiredArgsConstructor
@Log4j2
public class KafkaNotificationConsumer {
    private final KafkaReceiver<String, NotificationRequestDto> kafkaReceiver;
    private final NotificationService notificationService;

    @PostConstruct
    public void startConsuming() {
        kafkaReceiver.receive()
                .concatMap(receiverRecord -> {
                    NotificationRequestDto value = receiverRecord.value();
                    if (value == null) {
                        receiverRecord.receiverOffset().acknowledge(); // синхронно
                        return Mono.empty();
                    }

                    return notificationService.sendEmail(value.getEmail(), value.getTitle(), value.getDescription())
                            .doOnSuccess(v -> {
                                receiverRecord.receiverOffset().acknowledge();
                                log.info("Email sent and offset committed for {}", value.getEmail());
                            })
                            .onErrorResume(e -> {
                                log.error("Email send failed for {}: {}", value.getEmail(), e.getMessage());
                                return Mono.empty();
                            })
                            .then(Mono.empty());
                })
                .doOnError(e -> log.error("Error in Kafka consumer", e))
                .subscribe();
    }
}