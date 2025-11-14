package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.exchange.ExchangeRatesDto;
import ru.practicum.mapper.exchange.ExchangeRateMapper;
import ru.practicum.service.exchange.ExchangeService;
import reactor.kafka.receiver.KafkaReceiver;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaRateConsumer {
    private final ExchangeService exchangeService;
    private final ExchangeRateMapper exchangeRateMapper;
    private final KafkaReceiver<String, ExchangeRatesDto> kafkaReceiver;

    @PostConstruct
    public void startConsuming() {
        kafkaReceiver.receive()
                .doOnNext(receiverRecord -> {
                    receiverRecord.receiverOffset().acknowledge();
                })
                .flatMap(receiverRecord -> {
                    ExchangeRatesDto value = receiverRecord.value();
                    if (value == null) {
                        return Mono.empty();
                    }
                    Flux<ru.practicum.model.exchange.ExchangeRate> ratesFlux = Flux.fromIterable(value.getRates())
                            .map(exchangeRateMapper::exchangeRateDtoToExchangeRate);
                    return exchangeService.updateRatesFromGenerator(ratesFlux);
                })
                .doOnError(e -> log.error("Error while processing Kafka message", e))
                .subscribe(v -> log.info("Successfully updated rates from Kafka"));
    }
}