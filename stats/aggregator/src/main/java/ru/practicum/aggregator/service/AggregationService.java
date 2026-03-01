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
    private final Map<Long, Double> eventTotalWeights = new HashMap<>();
    private final Map<Long, Map<Long, Double>> minWeightsSums = new HashMap<>();

    public List<EventSimilarityAvro> processEvent(UserActionAvro actionAvro) {
        log.info("Начинается процесс обработки сообщения от кафки в Aggregator: {}", actionAvro);

        long eventId = actionAvro.getEventId();
        long userId = actionAvro.getUserId();
        Instant timestamp = actionAvro.getTimestamp();
        double weight = getWeightByAction(actionAvro.getActionType());

        Double currentMaxWeight = eventUserWeights
                .computeIfAbsent(eventId, k -> new HashMap<>())
                .get(userId);


        if (currentMaxWeight != null && weight <= currentMaxWeight) {
            log.debug("Увеличение коэффициента веса не увеличилось. Сохраненный вес: {}. Вес в запросе: {}",
                    currentMaxWeight, weight);
            return Collections.emptyList();
        }

        double previousWeight = currentMaxWeight != null ? currentMaxWeight : 0.0;
        eventUserWeights.get(eventId).put(userId, weight);

        updateEventTotalWeight(eventId, weight, previousWeight);

        return updateAndCalculateSimilarities(eventId, userId, weight, previousWeight, timestamp);
    }

    private void updateEventTotalWeight(long eventId, double newWeight, double previousWeight) {
        log.debug("Начинается обновление общего веса мероприятия: {}", eventId);
        double currentTotal = eventTotalWeights.getOrDefault(eventId, 0.0);
        double updatedTotal = currentTotal - previousWeight + newWeight;
        eventTotalWeights.put(eventId, updatedTotal);

        log.debug("Вес мероприятия изменился. Было: {}. Стало: {}", currentTotal, updatedTotal);
    }

    private List<EventSimilarityAvro> updateAndCalculateSimilarities(long updatedEventId, long userId, double newWeight,
                                                                     double previousWeight, Instant timestamp) {
        log.debug("Начинается процесс обновления схожести мероприятия: {} c другими мероприятиями.", updatedEventId);
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : eventUserWeights.entrySet()) {
            long otherEventId = entry.getKey();

            if (otherEventId == updatedEventId) {
                continue;
            }

            Double otherWeight = entry.getValue().get(userId);
            if (otherWeight == null) {
                continue;
            }

            double minWeightDelta = calculateMinWeightDelta(newWeight, previousWeight, otherWeight);
            if (minWeightDelta != 0) {
                updateMinWeightSum(updatedEventId, otherEventId, minWeightDelta);
            }

            double similarity = calculateSimilarity(updatedEventId, otherEventId);
            if (similarity > 0) {
                similarities.add(createEventSimilarity(updatedEventId, otherEventId, similarity, timestamp));
                log.debug("Добавлено сходство мероприятий: Наше мероприятие: {}. Другое мероприятие: {}. Коэффициент: {}",
                        updatedEventId, otherEventId, similarity);
            }


        }
        log.info("Обновленный список схожести мероприятий: {}", similarities);
        return similarities;
    }

    private double calculateMinWeightDelta(double newWeight, double previousWeight, double otherWeight) {
        double previousMin = Math.min(previousWeight, otherWeight);
        double newMin = Math.min(newWeight, otherWeight);
        return newMin - previousMin;
    }

    private void updateMinWeightSum(long eventA, long eventB, double delta) {
        log.debug("Начинается процесс обновления минимальных весов мероприятий");
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        double currentSum = minWeightsSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .getOrDefault(second, 0.0);

        minWeightsSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .put(second, currentSum + delta);

        log.debug("Обновленная сумма минимальных весов: {}", currentSum + delta);
    }

    private double calculateSimilarity(long eventA, long eventB) {
        log.debug("Начинается подсчет сходства мероприятий: {} и {}", eventA, eventB);
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        Double sMin = minWeightsSums
                .computeIfAbsent(first, k -> new HashMap<>())
                .get(second);

        if (sMin == null || sMin == 0) {
            return 0.0;
        }

        Double sA = eventTotalWeights.get(eventA);
        Double sB = eventTotalWeights.get(eventB);

        if (sA == null || sB == null || sA == 0 || sB == 0) {
            return 0.0;
        }

        double similarity = sMin / (Math.sqrt(sA) * Math.sqrt(sB));
        log.debug("Схожесть мероприятий: {}", similarity);

        return similarity;
    }

    private EventSimilarityAvro createEventSimilarity(long eventA, long eventB,
                                                      double similarity, Instant timestamp) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(similarity)
                .setTimestamp(timestamp)
                .build();
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
}
