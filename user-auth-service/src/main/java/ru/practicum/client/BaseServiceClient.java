package ru.practicum.client;

import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.exception.ServiceUnavailableException;

import java.time.Duration;

/**
 * Базовый клиент сервисов
 */
public abstract class BaseServiceClient {
    private final WebClient webClient;

    public BaseServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    protected abstract Logger getLogger();

    protected abstract String getServiceId();

    protected WebClient getWebClient() {
        return webClient;
    }

    protected String getGatewayUrl() {
        String gatewayUrl = System.getenv("GATEWAY_URL");
        if (gatewayUrl == null) {
            throw new IllegalStateException("GATEWAY_URL not set");
        }
        return gatewayUrl + "/" + getServiceId();
    }

    protected <T> Mono<T> performMono(HttpMethod method, String path, Object body, Class<T> responseType,
                                      String operation, String errorMsgPrefix, boolean useServiceException) {
        getLogger().info(operation);

        String baseUrl = getGatewayUrl();
        String fullUrl = baseUrl + path;
        getLogger().debug("Calling {} at: {}", getServiceId(), fullUrl);

        WebClient.RequestHeadersSpec<?> spec = getWebClient().method(method)
                .uri(fullUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            ((WebClient.RequestBodySpec) spec).bodyValue(body);
        }

        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    getLogger().error("{} service error during {}: {}", getServiceId(), operation, response.statusCode());
                    String msg = errorMsgPrefix + response.statusCode().value();
                    RuntimeException ex;
                    if (useServiceException) {
                        ex = new ServiceUnavailableException(
                                msg,
                                getServiceId(),
                                "Сервис " + getServiceId() + " вернул ошибку: " + response.statusCode());
                    } else {
                        ex = new IllegalStateException(msg);
                    }
                    return Mono.error(ex);
                })
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(r -> getLogger().info("{} succeeded", operation))
                .doOnError(e -> getLogger().error("Error during {}: {}", operation, e.getMessage(), e))
                .onErrorResume(e -> {
                    getLogger().error("Failed {} ", operation, e);
                    String msg = "Сервис " + getServiceId() + " временно недоступен: " + e.getMessage();
                    RuntimeException ex;
                    if (useServiceException) {
                        ex = new ServiceUnavailableException(
                                msg,
                                getServiceId(),
                                "Проверьте подключение к сервису " + getServiceId());
                    } else {
                        ex = new IllegalStateException(msg, e.getCause());
                    }
                    return Mono.error(ex);
                });
    }

    protected <T> Flux<T> performFlux(HttpMethod method, String path, Object body, Class<T> responseType,
                                      String operation, String errorMsgPrefix, boolean useServiceException) {
        getLogger().info(operation);

        String baseUrl = getGatewayUrl();
        String fullUrl = baseUrl + path;
        getLogger().debug("Calling {} at: {}", getServiceId(), fullUrl);

        WebClient.RequestHeadersSpec<?> spec = getWebClient().method(method)
                .uri(fullUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (body != null && (method == HttpMethod.POST || method == HttpMethod.PUT)) {
            ((WebClient.RequestBodySpec) spec).bodyValue(body);
        }

        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, response -> {
                    getLogger().error("{} service error during {}: {}", getServiceId(), operation, response.statusCode());
                    String msg = errorMsgPrefix + response.statusCode().value();
                    RuntimeException ex;
                    if (useServiceException) {
                        ex = new ServiceUnavailableException(
                                msg,
                                getServiceId(),
                                "Сервис " + getServiceId() + " вернул ошибку: " + response.statusCode());
                    } else {
                        ex = new IllegalStateException(msg);
                    }
                    return Mono.error(ex);
                })
                .bodyToFlux(responseType)
                .timeout(Duration.ofSeconds(10))
                .doOnComplete(() -> getLogger().info("{} completed successfully", operation))
                .doOnError(e -> getLogger().error("Error during {}: {}", operation, e.getMessage(), e))
                .onErrorResume(e -> {
                    getLogger().error("Failed {} ", operation, e);
                    String msg = "Сервис " + getServiceId() + " временно недоступен: " + e.getMessage();
                    RuntimeException ex;
                    if (useServiceException) {
                        ex = new ServiceUnavailableException(
                                msg,
                                getServiceId(),
                                "Проверьте подключение к сервису " + getServiceId());
                    } else {
                        ex = new IllegalStateException(msg, e.getCause());
                    }
                    return Flux.error(ex);
                });
    }
}
