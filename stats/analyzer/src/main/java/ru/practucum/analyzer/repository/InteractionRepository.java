package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practucum.analyzer.model.Interaction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

//    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);
//
//    boolean existsByUserIdAndEventId(Long userId, Long eventId);
//
//    List<Interaction> findAllByUserId(Long userId);
//
    List<Interaction> findAllByEventIdIn(Collection<Long> eventIds);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Interaction> findAllByUserId(Long userId);

    List<Interaction> findAllByEventId(Long eventId);

}
