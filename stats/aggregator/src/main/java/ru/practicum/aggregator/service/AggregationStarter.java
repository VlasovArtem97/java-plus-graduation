package ru.practicum.aggregator.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationStarter {

    private final AggregationService aggregationService;

    private final Consumer<String, SpecificRecordBase> consumer;
    private final Producer<String, SpecificRecordBase> producer;

    @Value("${kafka.topicAction}")
    private String topicUserAction;

    @Value("${kafka.topicEventsSimilarity}")
    private String topicEventsSimilarity;

    //флаг для остановки producer и consumer
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public void start() {

        log.info("Начинается работа по получению данных из топика: {} kafka", topicUserAction);
        try {
            consumer.subscribe(List.of(topicUserAction));
            while (!closed.get()) {
                ConsumerRecords<String, SpecificRecordBase> record = consumer.poll(Duration.ofMillis(100));
                if (record.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SpecificRecordBase> rec : record) {
                    record(rec);
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            if (!closed.get()) {
                throw ignored;
            }
            log.info("Консьюмер разбужен и завершает работу.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки действий пользователей", e);
        } finally {
            log.debug("Начинается закрытие producer и consumer");
            consumer.close(Duration.ofSeconds(10));
            producer.flush();
            producer.close();

        }
    }

    private void record(ConsumerRecord<String, SpecificRecordBase> rec) {
        log.debug("Получены данные: {} из топика: {} kafka", rec.value(), rec.topic());
        List<EventSimilarityAvro> eventSimilarityAvroList = aggregationService.processEvent((UserActionAvro) rec.value());
        if (!eventSimilarityAvroList.isEmpty()) {
            log.debug("Данные UserActionAvro обновлены: {}", eventSimilarityAvroList);
            for (EventSimilarityAvro e : eventSimilarityAvroList) {
                producer.send(new ProducerRecord<>(topicEventsSimilarity, e), (metadata, exception) -> {
                    if (exception != null) {
                        log.error("Не удалось отправить объект типа \"EventSimilarityAvro\": {}, в топик: [ {} ]",
                                e, topicEventsSimilarity);
                    } else {
                        log.debug(" Объект типа \"EventSimilarityAvro\": {}, успешно отправлен в топик: [ {} ]",
                                e, topicEventsSimilarity);
                    }
                });
            }
            producer.flush();
        } else {
            log.debug("Данные не были обновлены");
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Остановка AggregationStarter...");
        closed.set(true);
        consumer.wakeup();
    }
}
