package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.OperationCheckRequestDto;
import ru.practicum.dto.OperationCheckResponseDto;
import ru.practicum.dto.OperationHistoryResponseDto;
import ru.practicum.mapper.OperationMapper;
import ru.practicum.service.BlockerService;

import java.util.UUID;

@RestController
@RequestMapping("/api/blocker")
@RequiredArgsConstructor
@Slf4j
public class BlockerController {
    /**
     * Сервис проверки операций
     */
    private final BlockerService blockerService;

    /**
     * Маппер операций
     */
    private final OperationMapper operationMapper;

    @PostMapping("/check")
    public Mono<OperationCheckResponseDto> checkOperation(@RequestBody OperationCheckRequestDto requestDto) {
        log.info("Received operation check request: {}", requestDto.getOperationId());
        return Mono.just(requestDto)
                .map(operationMapper::operationCheckRequestDtoToOperationCheckRequest)
                .flatMap(blockerService::checkOperation)
                .map(operationMapper::operationCheckResponseToOperationCheckResponseDto);
    }

    @GetMapping("/history/{userId}")
    public Flux<OperationHistoryResponseDto> getOperationHistory(@PathVariable UUID userId) {
        log.info("Received operation history request for user: {}", userId);
        return blockerService.getOperationHistory(userId)
                .map(operationMapper::operationHistoryToOperationHistoryResponseDto);
    }
}
