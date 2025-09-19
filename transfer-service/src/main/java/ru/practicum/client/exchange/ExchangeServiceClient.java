package ru.practicum.client.exchange;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.exception.ServiceClientException;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    public Mono<ExchangeRateDto> getRate(String fromCurrency, String toCurrency) {
        return getExchangeServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/exchange/rate?from=" + fromCurrency + "&to=" + toCurrency;
            log.debug("Запрос на получение курса валют: {} -> {}", fromCurrency, toCurrency);

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "exchange-service",
                                            "getRate",
                                            "Ошибка при получении курса: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(ExchangeRateDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("exchange-service", "getRate", "Таймаут при получении курса"))
                    .doOnSuccess(rate -> log.info("Курс валют {} -> {} успешно получен: {}", fromCurrency, toCurrency, rate.getBuyRate()))
                    .doOnError(error -> log.error("Ошибка при получении курса валют {} -> {}: {}", fromCurrency, toCurrency, error.getMessage()));
        });
    }

    public Mono<ConvertResponseDto> convertCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {
        ConvertRequestDto requestDto = ConvertRequestDto.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .build();

        return getExchangeServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/exchange/convert";
            log.debug("Запрос на конвертацию валют: {} {} -> {}", amount, fromCurrency, toCurrency);

            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "exchange-service",
                                            "convertCurrency",
                                            "Ошибка при конвертации: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToMono(ConvertResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("exchange-service", "convertCurrency", "Таймаут при конвертации"))
                    .doOnSuccess(response -> log.info("Конвертация {} {} -> {} выполнена: {}", amount, fromCurrency, toCurrency, response.getConvertedAmount()))
                    .doOnError(error -> log.error("Ошибка при конвертации валют: {}", error.getMessage()));
        });
    }

    public Flux<ExchangeRateDto> getCurrentRates() {
        return getExchangeServiceUrl().flatMapMany(baseUrl -> {
            String url = baseUrl + "/api/exchange/rates";
            log.debug("Запрос на получение текущих курсов валют");

            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .defaultIfEmpty("")
                                    .flatMap(body -> Mono.error(ServiceClientException.internalError(
                                            "exchange-service",
                                            "getCurrentRates",
                                            "Ошибка при получении курсов: " + response.statusCode() + " - " + body
                                    )))
                    )
                    .bodyToFlux(ExchangeRateDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .onErrorMap(TimeoutException.class, ex ->
                            ServiceClientException.timeout("exchange-service", "getCurrentRates", "Таймаут при получении курсов"))
                    .doOnComplete(() -> log.info("Текущие курсы валют успешно получены"))
                    .doOnError(error -> log.error("Ошибка при получении курсов валют: {}", error.getMessage()));
        });
    }

    private Mono<String> getExchangeServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("exchange-service");
            if (instances == null || instances.isEmpty()) {
                throw ServiceClientException.unavailable("exchange-service", "discovery", "Сервис конвертации недоступен");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Обнаружен exchange-service по адресу: {}", url);
            return url;
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к exchange-service: {}", e.getMessage());
            return Mono.error(ServiceClientException.unavailable("exchange-service", "discovery", e.getMessage()));
        });
    }
}