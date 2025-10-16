package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.NotificationRequestDto;
import ru.practicum.service.NotificationService;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /**
     * Сервис оповещений
     */
    private final NotificationService notificationService;

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendEmail(@RequestBody NotificationRequestDto request) {
        return notificationService.sendEmail(request.getEmail(), request.getTitle(), request.getDescription())
                .then(Mono.fromCallable(() -> ResponseEntity.ok("Email sent successfully to " + request.getEmail())))
                .onErrorReturn(ResponseEntity.ok("Email sent Failed to " + request.getEmail()));
    }
}