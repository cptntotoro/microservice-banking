package ru.practicum.client.blocker;

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
import ru.practicum.client.blocker.dto.OperationCheckRequestDto;
import ru.practicum.client.blocker.dto.OperationCheckResponseDto;

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

    public Mono<OperationCheckResponseDto> checkOperation(OperationCheckRequestDto requestDto) {
        String path = "/api/blocker/check";
        String operation = "Check operation: " + requestDto;
        String errorPrefix = "Ошибка проверки операции: ";
        return performMono(HttpMethod.POST, path, requestDto, OperationCheckResponseDto.class, operation, errorPrefix, true)
                .doOnSuccess(response -> log.info("Operation checked"));
    }
}