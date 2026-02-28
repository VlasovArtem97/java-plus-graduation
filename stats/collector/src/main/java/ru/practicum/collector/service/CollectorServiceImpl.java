package ru.practicum.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.collector.mapper.UserActionMapper;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.proto.messages.UserActionProto;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService{

    private final Producer<String, SpecificRecordBase> producer;
    private final UserActionMapper userActionMapper;

    @Value("${kafka.topicAction}")
    private String topic;

    @Override
    public void addActionUser(UserActionProto actionProto) {
        validation(actionProto);
        UserActionAvro avro = userActionMapper.toUserActionAvro(actionProto);
        log.debug("Объект после маппинга из proto в avro: {}", avro);
        sendMessage(topic, avro);
    }

    private void sendMessage(String topic, SpecificRecordBase event) {
        try {
            ProducerRecord<String, SpecificRecordBase> record = new ProducerRecord<>(topic, event);
            producer.send(record).get(5, TimeUnit.SECONDS);
            log.debug("Объект: [ {} ] успешно отправлен в топик: [ {} ]", event, topic);
        } catch (Exception e) {
            log.error("Ошибка отправки объекта: {} в топик {}.", event, topic, e);
            throw new RuntimeException("Ошибка при отправки сообщения в kafka " + e.getMessage(), e);
        }
    }

    private void validation(UserActionProto actionProto) {
        if (actionProto == null) {
            throw new IllegalStateException("Ошибка валидации. UserAction равен null. Получаем: ");
        }
        if (actionProto.getUserId() < 1) {
            throw new IllegalStateException("Ошибка валидации. userId должен быть больше 0. Получаем: " +
                    actionProto.getUserId());
        }
        if (actionProto.getEventId() < 1) {
            throw new IllegalStateException("Ошибка валидации. eventId должен быть больше 0. Получаем: " +
                    actionProto.getEventId());
        }
        if (!actionProto.hasTimestamp()) {
            throw new IllegalStateException("Ошибка валидации. Timestamp обязателен.");
        }

        long seconds = actionProto.getTimestamp().getSeconds();
        long now = Instant.now().getEpochSecond() + 20;

        if (seconds > now) {
            throw new IllegalStateException("Ошибка валидации. Время события не может быть из будущего.");
        }

        if (seconds <= 0) {
            throw new IllegalStateException("Ошибка валидации. Некорректное значение секунд в timestamp.");
        }
    }
}
