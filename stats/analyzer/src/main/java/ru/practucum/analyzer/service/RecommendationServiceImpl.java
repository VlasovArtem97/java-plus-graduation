package ru.practucum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.proto.messages.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.messages.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.messages.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.messages.UserPredictionsRequestProto;
import ru.practucum.analyzer.model.Interaction;
import ru.practucum.analyzer.model.Similarity;
import ru.practucum.analyzer.repository.InteractionRepository;
import ru.practucum.analyzer.repository.SimilarityRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final SimilarityRepository similarityRepository;
    private final InteractionRepository interactionRepository;

    /**
     * АЛГОРИТМ 1: Поиск похожих мероприятий (GetSimilarEvents)
     */
    /**
     * Алгоритм предсказания оценки (Главная страница)
     */
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        long userId = request.getUserId();
        log.info("Запрос рекомендаций для пользователя: {}, max results: {}", userId, request.getMaxResults());

        List<Interaction> recent = interactionRepository.findRecent(userId, 20);
        if (recent.isEmpty()) {
            log.warn("История взаимодействий для пользователя {} пуста. Рекомендовать нечего.", userId);
            return List.of();
        }
        log.debug("Найдено последних взаимодействий: {}", recent.size());

        Map<Long, Float> userRatings = recent.stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating, (a, b) -> a));
        Set<Long> watchedIds = userRatings.keySet();

        List<Long> candidates = recent.stream()
                .flatMap(i -> similarityRepository.findAnySimilar(i.getEventId()).stream())
                .map(s -> watchedIds.contains(s.getEventA()) ? s.getEventB() : s.getEventA())
                .filter(id -> !watchedIds.contains(id))
                .distinct()
                .toList();

        log.debug("Найдено потенциальных кандидатов: {}", candidates.size());

        if (candidates.isEmpty()) return List.of();

        return candidates.stream().map(candId -> {
                    List<Similarity> neighbors = similarityRepository.findTopKNeighbors(candId, new ArrayList<>(watchedIds), 5);

                    double weightedSum = 0;
                    double simSum = 0;

                    for (Similarity s : neighbors) {
                        long wId = s.getEventA().equals(candId) ? s.getEventB() : s.getEventA();
                        float rating = userRatings.getOrDefault(wId, 0f);

                        weightedSum += rating * s.getScore();
                        simSum += s.getScore();
                    }

                    float finalScore = simSum == 0 ? 0 : (float) (weightedSum / simSum);
                    return RecommendedEventProto.newBuilder().setEventId(candId).setScore(finalScore).build();
                })
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Запрос похожих событий для eventId: {}, для пользователя: {}", request.getEventId(), request.getUserId());

        List<Similarity> similarities = similarityRepository.findAnySimilar(request.getEventId());
        log.debug("Найдено похожих пар в БД: {}", similarities.size());

        Set<Long> userHistory = interactionRepository.findAllByUserId(request.getUserId())
                .stream().map(Interaction::getEventId).collect(Collectors.toSet());

        List<RecommendedEventProto> result = similarities.stream()
                .map(s -> {
                    long targetId = s.getEventA().equals(request.getEventId()) ? s.getEventB() : s.getEventA();
                    return Map.entry(targetId, (float) s.getScore());
                })
                .filter(e -> !userHistory.contains(e.getKey()))
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();

        log.info("Возвращаю {} похожих событий", result.size());
        return result;
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> ids = request.getEventIdList();
        log.info("Запрос суммы взаимодействий для мероприятий: {}", ids);

        if (ids.isEmpty()) return List.of();

        List<Object[]> rawData = interactionRepository.sumRatingsByEventIds(ids);

        // Используем ((Number) row[1]).doubleValue() для безопасного приведения
        Map<Long, Double> results = rawData.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Number) row[1]).doubleValue(),
                        (a, b) -> a
                ));

        return ids.stream().map(id -> {
                    // В Proto score — это float, поэтому здесь конвертируем в floatValue()
                    float score = results.getOrDefault(id, 0.0).floatValue();
                    log.debug("Событие ID: {}, итоговый score: {}", id, score);
                    return RecommendedEventProto.newBuilder()
                            .setEventId(id)
                            .setScore(score)
                            .build();
                }
        ).toList();
    }
}
