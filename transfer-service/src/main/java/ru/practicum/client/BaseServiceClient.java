package ru.practicum.client;

import org.slf4j.Logger;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.exception.ServiceUnavailableException;

import java.time.Duration;
import java.util.List;

/**
 * Базовый клиент сервисов
 */
public abstract class BaseServiceClient {
    private final WebClient webClient;
    private final DiscoveryClient discoveryClient;

    public BaseServiceClient(WebClient webClient, DiscoveryClient discoveryClient) {
        this.webClient = webClient;
        this.discoveryClient = discoveryClient;
    }

    protected abstract Logger getLogger();
    protected abstract String getServiceId();

    protected WebClient getWebClient() {
        return webClient;
    }

    protected Mono<String> getServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("api-gateway-server");
            if (instances == null || instances.isEmpty()) {
                getLogger().error("No instances of {} found", getServiceId());
                throw new ServiceUnavailableException(
                        "Сервис " + getServiceId() + " не найден в реестре",
                        getServiceId(),
                        "Не удалось обнаружить экземпляры сервиса в Consul");
            }

            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            getLogger().debug("Discovered {} at: {}", getServiceId(), url);
            return url + "/" + getServiceId();
        }).onErrorResume(throwable -> {
            getLogger().error("Error discovering {}: {}", getServiceId(), throwable.getMessage(), throwable);
            return Mono.error(new ServiceUnavailableException(
                    "Ошибка обнаружения сервиса " + getServiceId(),
                    getServiceId(),
                    "Проблема с реестром сервисов: " + throwable.getMessage()));
        });
    }

    protected <T> Mono<T> performMono(HttpMethod method, String path, Object body, Class<T> responseType,
                                      String operation, String errorMsgPrefix, boolean useServiceException) {
        getLogger().info(operation);

        return getServiceUrl().flatMap(baseUrl -> {
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
                    .doOnError(e -> getLogger().error("Error during {}: {}", operation, e.getMessage(), e));
        }).onErrorResume(e -> {
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

        return getServiceUrl().flatMapMany(baseUrl -> {
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
                    .doOnError(e -> getLogger().error("Error during {}: {}", operation, e.getMessage(), e));
        }).onErrorResume(e -> {
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
