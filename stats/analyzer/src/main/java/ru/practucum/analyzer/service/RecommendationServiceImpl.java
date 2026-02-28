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

import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

        // 1. Получаем историю взаимодействий (в репозитории должен быть LIMIT N)
        List<Interaction> recent = interactionRepository.findRecent(userId, 20);
        if (recent.isEmpty()) return List.of();

        List<Long> watchedIds = recent.stream().map(Interaction::getEventId).toList();

        // 2. Поиск кандидатов (похожие на просмотренные, но сами не просмотрены)
        List<Long> candidates = recent.stream()
                .flatMap(i -> similarityRepository.findAnySimilar(i.getEventId()).stream())
                .map(s -> watchedIds.contains(s.getEventA()) ? s.getEventB() : s.getEventA())
                .filter(id -> !watchedIds.contains(id))
                .distinct()
                .toList();

        // 3. Расчет оценки по формуле взвешенного среднего
        return candidates.stream().map(candId -> {
                    // K ближайших соседей среди уже просмотренных пользователем мероприятий
                    List<Similarity> neighbors = similarityRepository.findTopKNeighbors(candId, watchedIds, 5);

                    double weightedSum = 0;
                    double simSum = 0;

                    for (Similarity s : neighbors) {
                        // Находим ID того события из пары, которое пользователь УЖЕ видел
                        long wId = s.getEventA().equals(candId) ? s.getEventB() : s.getEventA();

                        // Берем рейтинг этого события из истории
                        float userRating = recent.stream()
                                .filter(r -> r.getEventId().equals(wId))
                                .findFirst()
                                .map(Interaction::getRating).orElse(0f);

                        weightedSum += userRating * s.getScore();
                        simSum += s.getScore();
                    }

                    float finalScore = simSum == 0 ? 0 : (float) (weightedSum / simSum);
                    return RecommendedEventProto.newBuilder().setEventId(candId).setScore(finalScore).build();
                })
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .limit(request.getMaxResults())
                .toList();
    }

    /**
     * Поиск похожих мероприятий (для SimilarEventsRequestProto)
     */
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        // Получаем все пары, где участвует наше событие
        List<Similarity> similarities = similarityRepository.findAnySimilar(request.getEventId());

        // История пользователя для исключения
        List<Long> userHistory = interactionRepository.findAllByUserId(request.getUserId())
                .stream().map(Interaction::getEventId).toList();

        return similarities.stream()
                .map(s -> {
                    long targetId = s.getEventA().equals(request.getEventId()) ? s.getEventB() : s.getEventA();
                    return Map.entry(targetId, (float) s.getScore());
                })
                // ТЗ: Исключаем те, с которыми пользователь уже взаимодействовал
                .filter(e -> !userHistory.contains(e.getKey()))
                .sorted(Map.Entry.<Long, Float>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(e -> RecommendedEventProto.newBuilder()
                        .setEventId(e.getKey())
                        .setScore(e.getValue())
                        .build())
                .toList();
    }

    /**
     * Сумма максимальных весов взаимодействий
     */
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        return request.getEventIdList().stream().map(id -> {
            // В базе rating — это float, возвращаем сумму как float
            Double sum = interactionRepository.sumRatingByEventId(id);
            return RecommendedEventProto.newBuilder()
                    .setEventId(id)
                    .setScore(sum != null ? sum.floatValue() : 0.0f)
                    .build();
        }).toList();
    }
}
