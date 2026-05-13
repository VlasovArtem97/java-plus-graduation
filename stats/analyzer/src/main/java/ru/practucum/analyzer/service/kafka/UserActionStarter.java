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
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Slf4j
public class UserActionStarter implements Runnable {

    private final UserActionService userActionService;

    private final Consumer<String, SpecificRecordBase> consumer;

    @Value("${kafka.topicAction}")
    private String topicUserAction;

    private final AtomicBoolean closed = new AtomicBoolean(false);

    public UserActionStarter(@Qualifier("consumerUserAction") Consumer<String, SpecificRecordBase> consumer,
                             UserActionService userActionService) {
        this.consumer = consumer;
        this.userActionService = userActionService;
    }

    @Override
    public void run() {
        log.info("Начинается работа по получению данных из топика: {} kafka", topicUserAction);
        try {
            consumer.subscribe(List.of(topicUserAction));
            while (!closed.get()) {
                ConsumerRecords<String, SpecificRecordBase> record = consumer.poll(Duration.ofMillis(100));
                if (record.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, SpecificRecordBase> rec : record) {
                    try {
                        log.debug("Получен объект: {}", rec.value());
                        userActionService.saveUserAction((UserActionAvro) rec.value());
                    } catch (IllegalStateException e) {
                        log.warn("Ошибка в валидации данных: {}", e.getMessage());
                    }
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.warn("Приложение завершает работу по команде Wakeup");
        } catch (Exception e) {
            log.error("Ошибка во время обработки сообщений из Kafka", e);
        } finally {
            log.debug("Начинается закрытие consumer");
            consumer.close(Duration.ofSeconds(10));
        }
    }

    @PreDestroy
    public void stop() {
        log.info("Остановка Analyzer(UserActionStarter)");
        closed.set(true);
        consumer.wakeup();
    }
}
