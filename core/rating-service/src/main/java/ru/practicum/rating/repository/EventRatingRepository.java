package ru.practicum.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.rating.model.EventRating;

import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {

    Optional<EventRating> findByEventAndUser(Long eventId, Long userId);

    @Query("select coalesce(sum(er.value), 0) from EventRating er where er.event = ?1")
    Long sumScoreByEventId(Long eventId);

    @Query("select count(er) from EventRating er where er.event = ?1 and er.value = 1")
    Long countLikes(Long eventId);

    @Query("select count(er) from EventRating er where er.event = ?1 and er.value = -1")
    Long countDislikes(Long eventId);
}
