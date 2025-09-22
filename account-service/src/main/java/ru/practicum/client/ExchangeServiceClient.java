package ru.practicum.client;

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
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.exchange.ExchangeRequestDto;
import ru.practicum.dto.exchange.ExchangeResponseDto;

import java.time.Duration;
import java.util.List;

/**
 * Клиент для обращения к сервису обмена валют
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;

    /**
     * Получить актуальные курсы валют
     *
     * @return Список DTO курсов валют
     */
    public Flux<ExchangeRateDto> getCurrentRates() {
        return getExchangeServiceUrl().flatMapMany(baseUrl -> {
            String url = baseUrl + "/api/exchange/rates";
            log.debug("Получение курсов валют по URL: {}", url);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Ошибка получения курсов валют: {}", response.statusCode());
                        return Mono.error(new RuntimeException("Не удалось получить курсы валют"));
                    })
                    .bodyToFlux(ExchangeRateDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnNext(rate -> log.info("Получен курс валют: {}", rate))
                    .doOnError(error -> log.error("Ошибка при получении курсов валют: ", error));
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к exchange-service", e);
            return Flux.error(new RuntimeException("Exchange service недоступен: " + e.getMessage()));
        });
    }

    /**
     * Получить курс валют для конкретной пары
     *
     * @param from Код валюты отправителя
     * @param to Код валюты получателя
     * @return DTO курса валют
     */
    public Mono<ExchangeRateDto> getRate(String from, String to) {
        return getExchangeServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/exchange/rates/" + from + "/" + to;
            log.debug("Получение курса валют для пары {}/{} по URL: {}", from, to, url);
            return webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Ошибка получения курса для пары {}/{}: {}", from, to, response.statusCode());
                        return Mono.error(new RuntimeException("Не удалось получить курс валют"));
                    })
                    .bodyToMono(ExchangeRateDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(rate -> log.info("Получен курс для пары {}/{}", from, to))
                    .doOnError(error -> log.error("Ошибка при получении курса валют: ", error));
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к exchange-service", e);
            return Mono.error(new RuntimeException("Exchange service недоступен: " + e.getMessage()));
        });
    }

    /**
     * Выполнить конвертацию валют
     *
     * @param requestDto Запрос на конвертацию
     * @return DTO ответа конвертации
     */
    public Mono<ExchangeResponseDto> convertCurrency(ExchangeRequestDto requestDto) {
        return getExchangeServiceUrl().flatMap(baseUrl -> {
            String url = baseUrl + "/api/exchange/convert";
            log.debug("Запрос на конвертацию валют по URL: {}", url);
            return webClientBuilder.build()
                    .post()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestDto)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response -> {
                        log.error("Ошибка конвертации валют: {}", response.statusCode());
                        return Mono.error(new RuntimeException("Не удалось выполнить конвертацию"));
                    })
                    .bodyToMono(ExchangeResponseDto.class)
                    .timeout(Duration.ofSeconds(10))
                    .doOnSuccess(response -> log.info("Конвертация выполнена: {}", response))
                    .doOnError(error -> log.error("Ошибка при конвертации валют: ", error));
        }).onErrorResume(e -> {
            log.error("Не удалось подключиться к exchange-service", e);
            return Mono.error(new RuntimeException("Exchange service недоступен: " + e.getMessage()));
        });
    }

//    /**
//     * Получить список доступных валют
//     *
//     * @return DTO доступных валют
//     */
//    public Mono<AvailableCurrenciesDto> getAvailableCurrencies() {
//        return getExchangeServiceUrl().flatMap(baseUrl -> {
//            String url = baseUrl + "/api/exchange/currencies";
//            log.debug("Получение доступных валют по URL: {}", url);
//            return webClientBuilder.build()
//                    .get()
//                    .uri(url)
//                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                    .retrieve()
//                    .onStatus(HttpStatusCode::isError, response -> {
//                        log.error("Ошибка получения доступных валют: {}", response.statusCode());
//                        return Mono.error(new RuntimeException("Не удалось получить доступные валюты"));
//                    })
//                    .bodyToMono(AvailableCurrenciesDto.class)
//                    .timeout(Duration.ofSeconds(10))
//                    .doOnSuccess(currencies -> log.info("Получены доступные валюты: {}", currencies))
//                    .doOnError(error -> log.error("Ошибка при получении доступных валют: ", error));
//        }).onErrorResume(e -> {
//            log.error("Не удалось подключиться к exchange-service", e);
//            return Mono.error(new RuntimeException("Exchange service недоступен: " + e.getMessage()));
//        });
//    }

    private Mono<String> getExchangeServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("exchange-service");
            if (instances == null || instances.isEmpty()) {
                throw new RuntimeException("Экземпляры exchange-service не найдены");
            }
            ServiceInstance instance = instances.getFirst();
            String url = "http://" + instance.getHost() + ":" + instance.getPort();
            log.debug("Обнаружен exchange-service по адресу: {}", url);
            return url;
        });
    }
}