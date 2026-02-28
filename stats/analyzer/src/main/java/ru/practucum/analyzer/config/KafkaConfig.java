package ru.practucum.analyzer.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import ru.practicum.avrodeserialization.EventSimilarDeserializer;
import ru.practicum.avrodeserialization.UserActionDeserializer;

import java.util.Properties;

public class KafkaConfig {

    @Value("${kafka.port}")
    private String kafkaPort;

    @Value("${kafka.consumerClientIdForAnalyzerUserAction}")
    private String clientIdForUserAction;

    @Value("${kafka.consumerClientIdForAnalyzerEventSimilar}")
    private String clientIdForEventSimilar;

    @Value("${kafka.consumerGroupIdForAggregator}")
    private String groupId;

    @Bean("consumerUserAction")
    public Consumer<String, SpecificRecordBase> kafkaConsumerForUserAction() {
        Properties config = new Properties();
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, clientIdForUserAction);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPort);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserActionDeserializer.class.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000);
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        return new KafkaConsumer<>(config);
    }

    @Bean("consumerEventSimilar")
    public Consumer<String, SpecificRecordBase> kafkaConsumerForEventSimilar() {
        Properties config = new Properties();
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, clientIdForEventSimilar);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaPort);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EventSimilarDeserializer.class.getName());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, OffsetResetStrategy.EARLIEST.name().toLowerCase());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 30000);
        config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100);
        return new KafkaConsumer<>(config);
    }
}
