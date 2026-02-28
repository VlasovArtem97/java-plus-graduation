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

        // 1. Получаем историю (лимит 20 последних действий)
        List<Interaction> recent = interactionRepository.findRecent(userId, 20);
        if (recent.isEmpty()) return List.of();

        // Превращаем историю в Map для быстрого поиска рейтинга O(1)
        Map<Long, Float> userRatings = recent.stream()
                .collect(Collectors.toMap(Interaction::getEventId, Interaction::getRating, (a, b) -> a));
        Set<Long> watchedIds = userRatings.keySet();

        // 2. Поиск кандидатов (одним запросом собираем все похожие события для всей истории)
        List<Long> candidates = recent.stream()
                .flatMap(i -> similarityRepository.findAnySimilar(i.getEventId()).stream())
                .map(s -> watchedIds.contains(s.getEventA()) ? s.getEventB() : s.getEventA())
                .filter(id -> !watchedIds.contains(id))
                .distinct()
                .toList();

        if (candidates.isEmpty()) return List.of();

        // 3. Расчет оценок
        return candidates.stream().map(candId -> {
                    // Используем native query с LIMIT 5 для каждого кандидата
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
        List<Similarity> similarities = similarityRepository.findAnySimilar(request.getEventId());

        Set<Long> userHistory = interactionRepository.findAllByUserId(request.getUserId())
                .stream().map(Interaction::getEventId).collect(Collectors.toSet());

        return similarities.stream()
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
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        List<Long> ids = request.getEventIdList();
        if (ids.isEmpty()) return List.of();

        Map<Long, Double> results = interactionRepository.sumRatingsByEventIds(ids).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Double) row[1],
                        (a, b) -> a
                ));

        return ids.stream().map(id -> RecommendedEventProto.newBuilder()
                .setEventId(id)
                .setScore(results.getOrDefault(id, 0.0).floatValue())
                .build()
        ).toList();
    }
}
