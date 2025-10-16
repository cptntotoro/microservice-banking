package ru.practicum.controller.operation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.practicum.dto.operation.OperationDto;
import ru.practicum.mapper.operation.OperationMapper;
import ru.practicum.service.operation.OperationService;

@RestController
@RequestMapping("/api/operations")
@RequiredArgsConstructor
@Slf4j
public class OperationController {
    /**
     * Сервис управления валютными операциями
     */
    private final OperationService operationService;

    /**
     * Маппер валютных операций
     */
    private final OperationMapper operationMapper;

    @GetMapping("/recent")
    public Flux<OperationDto> getRecentOperations(@RequestParam(defaultValue = "10") int limit) {
        return operationService.getRecentOperations(limit)
                .map(operationMapper::operationToOperationDto);
    }

    @GetMapping("/pair/{from}/{to}")
    public Flux<OperationDto> getOperationsByCurrencyPair(
            @PathVariable String from,
            @PathVariable String to) {
        return operationService.getOperationsByCurrencyPair(from.toUpperCase(), to.toUpperCase())
                .map(operationMapper::operationToOperationDto);
    }

    @GetMapping("/currencies")
    public Flux<String> getAllUsedCurrencies() {
        return operationService.getAllUsedCurrencies();
    }
}