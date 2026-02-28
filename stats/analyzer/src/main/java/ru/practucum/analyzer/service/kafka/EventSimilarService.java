package ru.practucum.analyzer.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practucum.analyzer.model.Similarity;
import ru.practucum.analyzer.repository.SimilarityRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventSimilarService {

    private final SimilarityRepository similarityRepository;

    @Transactional
    public void saveEventSimilarity(EventSimilarityAvro avro) {
        log.info("Начинается процесс по сохранению сходства мероприятий: {}", avro);
        validate(avro);

        double score = Math.max(0.0, Math.min(1.0, avro.getScore()));

        long eventA = Math.min(avro.getEventA(), avro.getEventB());
        long eventB = Math.max(avro.getEventA(), avro.getEventB());

        Optional<Similarity> similarity = similarityRepository.findByEvent1AndEvent2(eventA, eventB);

        if (similarity.isEmpty()) {
            log.debug("Не было найдено схожесть мероприятий");
            similarityRepository.save(Similarity.builder()
                    .event1(eventA)
                    .event2(eventB)
                    .timestamp(avro.getTimestamp())
                    .similarity(score)
                    .build());
            log.debug("Схожесть мероприятий успешно сохранено");
        } else {
            Similarity updateSimilarity = similarity.get();
            log.debug("Схожесть мероприятий найдена: {}", updateSimilarity);
            updateSimilarity.setSimilarity(score);
            updateSimilarity.setTimestamp(avro.getTimestamp());
            similarityRepository.save(updateSimilarity);
            log.debug("Запись схожести мероприятия успешно обновлена");
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
    }
}
