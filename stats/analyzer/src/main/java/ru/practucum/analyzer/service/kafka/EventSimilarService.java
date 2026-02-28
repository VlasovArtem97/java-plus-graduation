package ru.practucum.analyzer.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practucum.analyzer.model.Similarity;
import ru.practucum.analyzer.repository.SimilarityRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarService {

    private final SimilarityRepository similarityRepository;

    public void saveEventSimilarity(EventSimilarityAvro avro) {
        log.info("Начинается процесс по сохранению сходства мероприятий: {}", avro);
        validate(avro);

        long eventA = Math.min(avro.getEventA(), avro.getEventB());
        long eventB = Math.max(avro.getEventA(), avro.getEventB());

        Optional<Similarity> similarity = similarityRepository.findByEventAAndEventB(eventA, eventB);
        if(similarity.isEmpty()) {
            log.debug("Не было найдено схожесть мероприятий");
            similarityRepository.save(Similarity.builder()
                            .eventA(eventA)
                            .eventB(eventB)
                            .instant(avro.getTimestamp())
                            .score(avro.getScore())
                    .build());
            log.debug("Схожесть мероприятий успешно сохранено");
        } else {
            Similarity updateSimilarity = similarity.get();
            log.debug("Схожесть мероприятий найдена: {}", updateSimilarity);
            if(avro.getScore() > updateSimilarity.getScore()) {
                updateSimilarity.setScore(avro.getScore());
                updateSimilarity.setInstant(avro.getTimestamp());
                similarityRepository.save(updateSimilarity);
                log.debug("Запись схожести мероприятия успешно обновлена");
            } else {
                log.warn("Коэффициент схожести мероприятия в переданном объекте ниже, чем в сохраненном. " +
                        "Переданный коэфициент: [ {} ]. Сохраненный: [ {} ]", avro.getScore(), updateSimilarity.getScore());
            }
        }
    }

    private void validate(EventSimilarityAvro avro) {
        if (avro == null) {
            throw new IllegalStateException("Переданный объект равен null");
        }
        if (avro.getEventA() == avro.getEventB()) {
            throw new IllegalStateException("EventA and EventB равны. EventA: [ " + avro.getEventA() + " ]. " +
                    "EventB: [ " + avro.getEventB() + " ]");
        }
        if (avro.getScore() < 0 || avro.getScore() > 1) {
            throw new IllegalStateException("Score должен быть в диапазоне [0, 1]. Получено: " + avro.getScore());
        }
    }
}
