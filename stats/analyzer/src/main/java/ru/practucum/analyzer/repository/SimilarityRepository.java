package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practucum.analyzer.model.Similarity;

import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    Optional<Similarity> findByEventAAndEventB(Long eventA, Long eventB);

    @Query("SELECT s FROM Similarity s WHERE s.event_a = :id OR s.event_b = :id")
    List<Similarity> findAnySimilar(@Param("id") Long eventId);

    @Query(value = "SELECT * FROM similarities " +
            "WHERE (event_a = :c AND event_b IN :w) " +
            "OR (event_b = :c AND event_a IN :w) " +
            "ORDER BY score DESC LIMIT :k", nativeQuery = true)
    List<Similarity> findTopKNeighbors(@Param("c") Long candidateId, @Param("w") List<Long> watchedIds, @Param("k") int k);
}
