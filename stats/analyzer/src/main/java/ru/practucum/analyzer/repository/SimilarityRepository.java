package ru.practucum.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practucum.analyzer.model.Similarity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

@Query("SELECT s FROM Similarity s WHERE s.event1 IN :ids OR s.event2 IN :ids")
List<Similarity> findByEventIds(@Param("ids") Collection<Long> eventIds);

    Optional<Similarity> findByEvent1AndEvent2(Long event1, Long event2);
}
