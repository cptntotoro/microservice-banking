package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.CashRequestDto;
import ru.practicum.dto.CashResponseDto;
import ru.practicum.mapper.CashMapper;
import ru.practicum.service.CashService;

@RestController
@RequestMapping("/api/cash")
@RequiredArgsConstructor
public class CashController {
    /**
     * Сервис пополнения счета
     */
    private final CashService cashService;

    /**
     * Маппер запросов на пополнение счета
     */
    private final CashMapper cashMapper;

    @PostMapping("/deposit")
    public Mono<ResponseEntity<CashResponseDto>> deposit(@RequestBody CashRequestDto requestDto) {
        return cashService.deposit(cashMapper.toModel(requestDto))
                .map(cashMapper::toDto)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(new CashResponseDto("ERROR", e.getMessage()))))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(500).body(new CashResponseDto("ERROR", "Internal server error"))));
    }

    @PostMapping("/withdraw")
    public Mono<ResponseEntity<CashResponseDto>> withdraw(@RequestBody CashRequestDto requestDto) {
        return cashService.withdraw(cashMapper.toModel(requestDto))
                .map(cashMapper::toDto)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(new CashResponseDto("ERROR", e.getMessage()))))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.status(500).body(new CashResponseDto("ERROR", "Internal server error"))));
    }
}