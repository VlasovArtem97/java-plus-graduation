package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practucum.analyzer.model.Interaction;

import java.util.List;
import java.util.Optional;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    Optional<Interaction> findByEventIdAndUserId(Long eventId, Long userId);

    @Query(value = "SELECT * FROM interactions WHERE user_id = :u ORDER BY instant DESC LIMIT :l", nativeQuery = true)
    List<Interaction> findRecent(@Param("u") Long userId, @Param("l") int limit);

    @Query("SELECT SUM(i.rating) FROM Interaction i WHERE i.eventId = :e")
    Double sumRatingByEventId(@Param("e") Long eventId);

    List<Interaction> findAllByUserId(long userId);
}
