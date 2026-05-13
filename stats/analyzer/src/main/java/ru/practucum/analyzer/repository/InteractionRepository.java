package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practucum.analyzer.model.Interaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {


    List<Interaction> findAllByEventIdIn(Collection<Long> eventIds);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    List<Interaction> findAllByUserId(Long userId);


}
