package ru.practucum.analyzer.service.kafka;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class EventSimilarStarter {

    private final Consumer<String, SpecificRecordBase> consumer;
    private final EventSimilarService eventSimilarService;

    @Value("${kafka.topicEventsSimilarity}")
    private String topicEventSimilarity;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public EventSimilarStarter(@Qualifier("consumerEventSimilar") Consumer<String, SpecificRecordBase> consumer,
                               EventSimilarService eventSimilarService) {
        this.consumer = consumer;
        this.eventSimilarService = eventSimilarService;
    }

    public void start() {

        log.info("Начинается работа по получению данных из топика: {} kafka", topicEventSimilarity);
        try {
            consumer.subscribe(List.of(topicEventSimilarity));
            while (!closed.get()) {
                ConsumerRecords<String, SpecificRecordBase> record = consumer.poll(Duration.ofMillis(100));
                if (record.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SpecificRecordBase> rec : record) {
                    try {
                        log.debug("Получен объект: {}", rec.value());
                        eventSimilarService.saveEventSimilarity((EventSimilarityAvro) rec.value());
                    } catch (IllegalStateException e) {
                        log.warn("Ошибка в валидации данных: {}", e.getMessage());
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.warn("consumer начинает завершать работу.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки действий пользователей", e);
        } finally {
            log.debug("Начинается закрытие consumer");
            consumer.close(Duration.ofSeconds(10));
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Остановка AnalyzerStarter");
        closed.set(true);
        consumer.wakeup();
    }
}
