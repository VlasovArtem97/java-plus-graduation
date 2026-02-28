package ru.practicum.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
public class AggregationService {

    private static final double WEIGHT_VIEW = 0.4;
    private static final double WEIGHT_REGISTER = 0.8;
    private static final double WEIGHT_LIKE = 1.0;

    private final Map<Long, Map<Long, Double>> eventUserWeights = new HashMap<>();
    private final Map<Long, Set<Long>> userEventsHistory = new HashMap<>();
    private final Map<Long, Double> eventTotalWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> processEvent(UserActionAvro actionAvro) {
        log.info("Обработка события из Kafka: userId={}, eventId={}, action={}",
                actionAvro.getUserId(), actionAvro.getEventId(), actionAvro.getActionType());

        long eventId = actionAvro.getEventId();
        long userId = actionAvro.getUserId();
        Instant instant = actionAvro.getTimestamp();

        double newWeight = getWeightByAction(actionAvro.getActionType());

        Map<Long, Double> userWeights = eventUserWeights.computeIfAbsent(eventId, e -> new HashMap<>());
        double oldMaxWeight = userWeights.getOrDefault(userId, 0.0);

        if (newWeight <= oldMaxWeight) {
            log.info("Пропуск обработки: новый вес {} не больше старого макс. веса {} для пользователя {} и события {}",
                    newWeight, oldMaxWeight, userId, eventId);
            return Collections.emptyList();
        }

        log.debug("Обновление веса для пользователя {} по событию {}: {} -> {}", userId, eventId, oldMaxWeight, newWeight);
        userWeights.put(userId, newWeight);

        userEventsHistory.computeIfAbsent(userId, u -> new HashSet<>()).add(eventId);

        double deltaWeight = newWeight - oldMaxWeight;
        double currentTotal = eventTotalWeights.getOrDefault(eventId, 0.0);
        eventTotalWeights.put(eventId, currentTotal + deltaWeight);

        log.debug("Общий вес события {} обновлен: {} -> {}", eventId, currentTotal, eventTotalWeights.get(eventId));

        List<EventSimilarityAvro> similarities = updateSimilarities(userId, eventId, oldMaxWeight, newWeight, instant);
        log.info("Сгенерировано {} обновлений сходства для события {}", similarities.size(), eventId);

        return similarities;
    }

    private List<EventSimilarityAvro> updateSimilarities(long userId, long currentEventId, double oldWeight,
                                                         double newWeight, Instant timestamp) {
        Set<Long> otherEventsIds = userEventsHistory.getOrDefault(userId, Collections.emptySet());
        log.debug("У пользователя {} в истории {} событий. Расчет обновлений сходства...", userId, otherEventsIds.size());

        return otherEventsIds.stream()
                .filter(otherId -> !otherId.equals(currentEventId))
                .map(otherId -> {
                    double weightInOther = eventUserWeights.get(otherId).get(userId);
                    double deltaMinSum = Math.min(newWeight, weightInOther) - Math.min(oldWeight, weightInOther);

                    if (deltaMinSum != 0) {
                        log.trace("Обновление S_min для пары ({}, {}), дельта={}", currentEventId, otherId, deltaMinSum);
                        updateMinWeightsSum(currentEventId, otherId, deltaMinSum);

                        double score = calculateCosineSimilarity(currentEventId, otherId);
                        log.debug("Новый коэффициент сходства для ({}, {}): {}", currentEventId, otherId, score);

                        return getEventSimilarityAvro(currentEventId, otherId, score, timestamp);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private void updateMinWeightsSum(long eventA, long eventB, double delta) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Map<Long, Double> internalMap = minWeightsSums.computeIfAbsent(first, e -> new HashMap<>());
        double currentSMin = internalMap.getOrDefault(second, 0.0);
        internalMap.put(second, currentSMin + delta);
    }

    private double calculateCosineSimilarity(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        double sMin = minWeightsSums.getOrDefault(first, Collections.emptyMap()).getOrDefault(second, 0.0);
        double sA = eventTotalWeights.getOrDefault(eventA, 0.0);
        double sB = eventTotalWeights.getOrDefault(eventB, 0.0);

        if (sA == 0 || sB == 0) {
            log.warn("Ошибка расчета: sA={} или sB={} равны нулю. Возвращаю результат 0.0", sA, sB);
            return 0.0;
        }

        return sMin / (Math.sqrt(sA) * Math.sqrt(sB));
    }

    private double getWeightByAction(ActionTypeAvro avro) {
        return switch (avro) {
            case ActionTypeAvro.LIKE -> WEIGHT_LIKE;
            case ActionTypeAvro.REGISTER -> WEIGHT_REGISTER;
            case ActionTypeAvro.VIEW -> WEIGHT_VIEW;
            case null, default -> {
                log.error("Получен неизвестный тип действия: {}", avro);
                throw new IllegalStateException("Некорректно указан тип действия пользователя(UserAction)");
            }
        };
    }

    private EventSimilarityAvro getEventSimilarityAvro(long idA, long idB, double score, Instant timestamp) {
        long eventA = Math.min(idA, idB);
        long eventB = Math.max(idA, idB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(eventA)
                .setEventB(eventB)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }
}
