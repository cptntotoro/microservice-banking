package ru.practicum.client.blocker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.client.BaseServiceClient;
import ru.practicum.client.blocker.dto.OperationCheckRequestDto;
import ru.practicum.client.blocker.dto.OperationCheckResponseDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Клиент для обращений к сервису блокировки операций
 */
@Component
@Slf4j
public class BlockerServiceClient extends BaseServiceClient {

    @Autowired
    public BlockerServiceClient(@Qualifier("blockerServiceWebClient") WebClient webClient, DiscoveryClient discoveryClient) {
        super(webClient, discoveryClient);
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    @Override
    protected String getServiceId() {
        return "blocker-service";
    }

    /**
     * Проверяет операцию на блокировку
     * @return true если операция заблокирована, false если разрешена
     */
    public Mono<Boolean> checkOperation(OperationCheckRequestDto requestDto) {
        String path = "/api/blocker/check";
        String operation = "Check operation: " + requestDto;
        String errorPrefix = "Ошибка проверки операции: ";
        return performMono(HttpMethod.POST, path, requestDto, OperationCheckResponseDto.class, operation, errorPrefix, true)
                .map(this::processCheckResponse)
                .doOnNext(blocked -> logOperationCheckResult(blocked, requestDto))
                .doOnSuccess(response -> log.info("Operation checked"));
    }

    private Boolean processCheckResponse(OperationCheckResponseDto response) {
        log.debug("Ответ проверки операции: заблокировано={}, кодПричины={}, оценкаРиска={}",
                response.isBlocked(), response.getReasonCode(), response.getRiskScore());
        return response.isBlocked();
    }

    private void logOperationCheckResult(Boolean blocked, OperationCheckRequestDto request) {
        if (Boolean.TRUE.equals(blocked)) {
            log.warn("Операция ЗАБЛОКИРОВАНА: accountId={}, userId={}, type={}, amount={} {}",
                    request.getAccountId(), request.getUserId(), request.getOperationType(),
                    request.getAmount(), request.getCurrency());
        } else {
            log.info("Операция РАЗРЕШЕНА: accountId={}, userId={}, type={}",
                    request.getAccountId(), request.getUserId(), request.getOperationType());
        }
    }
}