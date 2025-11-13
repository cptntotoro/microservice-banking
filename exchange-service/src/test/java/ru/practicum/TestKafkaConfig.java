package ru.practicum;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import ru.practicum.dto.exchange.ExchangeRatesDto;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestKafkaConfig {

    @Bean
    public KafkaSender<String, ExchangeRatesDto> kafkaSender(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "0");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, false);

        SenderOptions<String, ExchangeRatesDto> senderOptions = SenderOptions.create(props);
        return KafkaSender.create(senderOptions);
    }

    @Bean("testKafkaReceiver")
    public KafkaReceiver<String, ExchangeRatesDto> kafkaReceiver(KafkaProperties properties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-exchange-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        ReceiverOptions<String, ExchangeRatesDto> options = ReceiverOptions.<String, ExchangeRatesDto>create(props)
                .subscription(java.util.Collections.singleton("exchange-rates"));

        return KafkaReceiver.create(options);
    }
}