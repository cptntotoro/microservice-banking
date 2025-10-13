package ru.practicum.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.NotificationRequest;
import ru.practicum.service.NotificationService;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendEmail(@RequestBody NotificationRequest request) {
        return notificationService.sendEmail(request.getEmail(), request.getTitle(), request.getDescription())
                .then(Mono.fromCallable(() -> ResponseEntity.ok("Email sent successfully to " + request.getEmail())))
                .onErrorReturn(ResponseEntity.ok("Email sent Failed to " + request.getEmail()));
        //TODO
//            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
    }
}