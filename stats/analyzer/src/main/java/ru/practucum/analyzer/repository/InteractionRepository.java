package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practucum.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = "SELECT * FROM interactions WHERE user_id = :u ORDER BY timestamp DESC LIMIT :l", nativeQuery = true)
    List<Interaction> findRecent(@Param("u") Long userId, @Param("l") int limit);


    @Query(value = "SELECT event_id, SUM(max_rating) FROM (" +
            "  SELECT event_id, user_id, MAX(rating) as max_rating " +
            "  FROM interactions " +
            "  WHERE event_id IN :ids " +
            "  GROUP BY event_id, user_id" +
            ") as subquery GROUP BY event_id", nativeQuery = true)
    List<Object[]> sumRatingsByEventIds(@Param("ids") List<Long> eventIds);

    List<Interaction> findAllByUserId(long userId);
}
