package ru.practucum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final SimilarityRepository similarityRepository;
    private final InteractionRepository interactionRepository;

    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Long userId = request.getUserId();
        log.info("Запрос рекомендаций для userId={}, maxResults={}", userId, request.getMaxResults());

        List<Interaction> userInteractions = interactionRepository.findAllByUserId(userId);
        if (userInteractions.isEmpty()) {
            log.debug("Для пользователя {} нет истории взаимодействий", userId);
            return List.of();
        }
        log.debug("Найдены взаимодействий {} для пользователя {}", userInteractions, userId);

        Set<Long> interactedIds = userInteractions.stream()
                .map(Interaction::getEventId).collect(Collectors.toSet());

        List<Similarity> similarities = similarityRepository.findByEventIds(interactedIds);
        log.debug("Из БД получены записи: {} о сходстве для пользователя {}", similarities, userId);

        Map<Long, Double> candidates = new HashMap<>();
        for (Similarity sim : similarities) {
            Long candidate = null;
            // Определяем, кто из пары является кандидатом
            if (interactedIds.contains(sim.getEvent1())) candidate = sim.getEvent2();
            else if (interactedIds.contains(sim.getEvent2())) candidate = sim.getEvent1();

            if (candidate != null && !interactedIds.contains(candidate)) {
                candidates.merge(candidate, sim.getSimilarity(), Math::max);
            }
        }
        log.debug("После фильтрации найдено {} потенциальных кандидатов", candidates.size());

        List<RecommendedEventProto> result = candidates.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(e -> toProto(e.getKey(), e.getValue()))
                .toList();

        log.info("Успешно сформировано рекомендации: {} для пользователя {}", result, userId);
        return result;
    }

    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        log.info("Поиск похожих событий для eventId={}, userId={}", request.getEventId(), request.getUserId());

        List<Similarity> similarities = similarityRepository.findByEventIds(List.of(request.getEventId()));
        if (similarities.isEmpty()) {
            log.debug("Похожих событий для {} не найдено", request.getEventId());
            return List.of();
        }

        Set<Long> userInteracted = (request.getUserId() != 0)
                ? interactionRepository.findAllByUserId(request.getUserId()).stream()
                .map(Interaction::getEventId).collect(Collectors.toSet())
                : Collections.emptySet();

        List<RecommendedEventProto> result = similarities.stream()
                .map(s -> new AbstractMap.SimpleEntry<>(s.getOtherEventId(request.getEventId()), s.getSimilarity()))
                .filter(entry -> !userInteracted.contains(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(request.getMaxResults())
                .map(entry -> toProto(entry.getKey(), entry.getValue()))
                .toList();

        log.debug("Для события {} найдены {} похожих вариантов после фильтрации", request.getEventId(), result);
        return result;
    }

    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        log.info("Запрос суммы взаимодействий для события: {}", request);

        List<Interaction> allInteractions = interactionRepository.findAllByEventIdIn(request.getEventIdList());
        log.debug("Для списка событий получены взаимодействия из БД: {}", allInteractions);

        Map<Long, List<Interaction>> grouped = allInteractions.stream()
                .collect(Collectors.groupingBy(Interaction::getEventId));

        List<RecommendedEventProto> recommendedEventProtos = request.getEventIdList().stream()
                .distinct()
                .map(id -> toProto(id, calculateScoreForEvent(grouped.getOrDefault(id, List.of()))))
                .sorted(Comparator.comparing(RecommendedEventProto::getScore).reversed())
                .toList();
        log.debug("Возвращенный объект с событиями: {}", recommendedEventProtos);
        return recommendedEventProtos;
    }

    private double calculateScoreForEvent(List<Interaction> interactions) {
        try {
            return interactions.stream()
                    .collect(Collectors.groupingBy(Interaction::getUserId,
                            Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Interaction::getRating)),
                                    opt -> opt.map(Interaction::getRating).orElse(0.0))))
                    .values().stream().mapToDouble(Double::doubleValue).sum();
        } catch (Exception e) {
            log.error("Ошибка при расчете скоринга для списка взаимодействий размером {}", interactions.size(), e);
            return 0.0;
        }
    }

    private RecommendedEventProto toProto(Long id, double score) {
        return RecommendedEventProto.newBuilder().setEventId(id).setScore(score).build();
    }
}
