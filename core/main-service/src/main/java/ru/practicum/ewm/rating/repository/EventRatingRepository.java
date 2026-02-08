package ru.practicum.ewm.rating.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.rating.model.EventRating;

import java.util.Optional;

public interface EventRatingRepository extends JpaRepository<EventRating, Long> {
    Optional<EventRating> findByEvent_IdAndUser_Id(Long eventId, Long userId);

    @Query("select coalesce(sum(er.value), 0) from EventRating er where er.event.id = ?1")
    Long sumScoreByEventId(Long eventId);

    @Query("select count(er) from EventRating er where er.event.id = ?1 and er.value = 1")
    Long countLikes(Long eventId);

    @Query("select count(er) from EventRating er where er.event.id = ?1 and er.value = -1")
    Long countDislikes(Long eventId);

    @Query("select coalesce(sum(er.value), 0) " +
            "from EventRating er " +
            "where er.event.initiator.id = :userId")
    Long sumScoreByUserId(Long userId);

}
