package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;
import ru.practicum.TestKafkaConfig;
import ru.practicum.dto.exchange.ExchangeRateDto;
import ru.practicum.dto.exchange.ExchangeRatesDto;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@DirtiesContext
@Import(TestKafkaConfig.class)
@EmbeddedKafka(partitions = 1, topics = {"exchange-rates"})
public class ExchangeKafkaIntegrationTest {

    private static final String TOPIC = "exchange-rates";
    private static final String KEY = "rates";

    @Autowired
    private KafkaSender<String, ExchangeRatesDto> kafkaSender;

    @Autowired
    @Qualifier("testKafkaReceiver")
    private KafkaReceiver<String, ExchangeRatesDto> kafkaReceiver;

    private BlockingQueue<ExchangeRatesDto> consumedMessages;

    @BeforeEach
    void setUp() {
        consumedMessages = new LinkedBlockingQueue<>();
        startConsumer();
    }

    @Test
    void shouldDeliverMessagesInOrder_AtMostOnce_AndSkipOldOnRestart() throws Exception {
        // Step 1: Send 3 messages
        ExchangeRatesDto msg1 = createRatesDto("75.00", "76.00");
        ExchangeRatesDto msg2 = createRatesDto("75.10", "76.10");
        ExchangeRatesDto msg3 = createRatesDto("75.20", "76.20");

        send(msg1).block(Duration.ofSeconds(5));
        send(msg2).block(Duration.ofSeconds(5));
        send(msg3).block(Duration.ofSeconds(5));

        await().atMost(15, TimeUnit.SECONDS)
                .until(() -> consumedMessages.size() >= 3);

        List<ExchangeRatesDto> firstBatch = new ArrayList<>(consumedMessages);
        assertThat(firstBatch).hasSize(3);
        assertThat(firstBatch.get(0).getRates().get(0).getBuyRate()).isEqualTo(new BigDecimal("75.00"));
        assertThat(firstBatch.get(2).getRates().get(0).getBuyRate()).isEqualTo(new BigDecimal("75.20"));

        // Step 2: Restart consumer (simulate crash)
        consumedMessages.clear();

        startConsumer(); // New consumer with auto.offset.reset=latest

        // Step 3: Send new message
        ExchangeRatesDto msg4 = createRatesDto("80.00", "81.00");
        send(msg4).block(Duration.ofSeconds(5));

        // Should receive ONLY the last one
        await().atMost(10, TimeUnit.SECONDS)
                .until(() -> !consumedMessages.isEmpty());

        ExchangeRatesDto received = consumedMessages.poll(5, TimeUnit.SECONDS);
        assertThat(received).isNotNull();
        assertThat(received.getRates().get(0).getBuyRate()).isEqualTo(new BigDecimal("80.00"));
        assertThat(consumedMessages).isEmpty();
    }

    private void startConsumer() {
        kafkaReceiver.receive()
                .doOnNext(record -> {
                    consumedMessages.add(record.value());
                    record.receiverOffset().acknowledge(); // At most once
                })
                .doOnError(e -> System.err.println("Consumer error: " + e.getMessage()))
                .subscribe();
    }

    private Mono<SenderResult<Integer>> send(ExchangeRatesDto dto) {
        SenderRecord<String, ExchangeRatesDto, Integer> record =
                SenderRecord.create(new org.apache.kafka.clients.producer.ProducerRecord<>(
                        TOPIC, KEY, dto), 1);
        return kafkaSender.send(Mono.just(record)).next();
    }

    private ExchangeRatesDto createRatesDto(String buy, String sell) {
        ExchangeRateDto rate = new ExchangeRateDto();
        rate.setBaseCurrency("USD");
        rate.setTargetCurrency("RUB");
        rate.setBuyRate(new BigDecimal(buy));
        rate.setSellRate(new BigDecimal(sell));

        ExchangeRatesDto dto = new ExchangeRatesDto();
        dto.setRates(List.of(rate));
        return dto;
    }
}