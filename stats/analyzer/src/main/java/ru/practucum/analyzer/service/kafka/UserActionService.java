package ru.practucum.analyzer.service.kafka;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practucum.analyzer.model.Interaction;
import ru.practucum.analyzer.repository.InteractionRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserActionService {

    private final InteractionRepository interactionRepository;

    private static final double WEIGHT_VIEW = 0.4;
    private static final double WEIGHT_REGISTER = 0.8;
    private static final double WEIGHT_LIKE = 1.0;

    @Transactional
    public void saveUserAction(UserActionAvro avro) {
        log.info("Начинается процесс по сохранению действий пользователя");
        validate(avro);
        double rating = getWeightByAction(avro.getActionType());
        Optional<Interaction> interaction = interactionRepository.findByUserIdAndEventId(avro.getUserId(),
                avro.getEventId());
        if (interaction.isEmpty()) {
            log.debug("Запись взаимодействия пользователя с мероприятием не найдена. создается новая запись");
            Interaction interaction1 = interactionRepository.save(Interaction.builder()
                    .userId(avro.getUserId())
                    .eventId(avro.getEventId())
                    .timestamp(avro.getTimestamp())
                    .rating(rating)
                    .build());
            log.debug("Запись взаимодействия пользователя с мероприятием успешно сохранено: {}", interaction1);
        } else {
            log.debug("найдена запись взаимодействия пользователя с мероприятием: {}", interaction);
            Interaction oldInteraction = interaction.get();
            if (oldInteraction.getRating() > rating) {
                log.warn("коэффициент нового взаимодействия ниже, чем сохраненный. Запись не обновляется");
                return;
            }
            oldInteraction.setRating(rating);
            oldInteraction.setTimestamp(avro.getTimestamp());
            Interaction interaction1 = interactionRepository.save(oldInteraction);
            log.debug("Запись взаимодействия пользователя с мероприятием успешно обновлена: {}", interaction1);
        }
    }

    private void validate(UserActionAvro avro) {
        if (avro == null) {
            throw new IllegalStateException("Переданный объект равен null");
        }
        if (avro.getUserId() < 1) {
            throw new IllegalStateException("UserId меньше нуля: " + avro.getUserId());
        }
        if (avro.getEventId() < 1) {
            throw new IllegalStateException("EventId меньше нуля: " + avro.getEventId());
        }
        if (avro.getActionType() == null) {
            throw new IllegalStateException("ActionType == null: " + avro.getActionType());
        }
        if (avro.getTimestamp() == null) {
            throw new IllegalStateException("Timestamp == null: " + avro.getActionType());
        }
        if (avro.getTimestamp().isAfter(Instant.now().plus(Duration.ofMinutes(2)))) {
            throw new IllegalStateException("Timestamp указан в будущем времени: " + avro.getTimestamp());
        }
    }

    private double getWeightByAction(ActionTypeAvro avro) {
        return switch (avro) {
            case ActionTypeAvro.LIKE -> WEIGHT_LIKE;
            case ActionTypeAvro.REGISTER -> WEIGHT_REGISTER;
            case ActionTypeAvro.VIEW -> WEIGHT_VIEW;
            case null, default ->
                    throw new IllegalStateException("Некорректно указан тип действия пользователя(UserAction)");
        };
    }
}
