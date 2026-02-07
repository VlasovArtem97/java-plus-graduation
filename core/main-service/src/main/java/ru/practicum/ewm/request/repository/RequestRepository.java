package ru.practicum.ewm.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    Long countByEventIdAndRequestStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(Long requesterId);

    Request findByIdAndRequesterId(Long requestId, Long requesterId);

    @EntityGraph(attributePaths = {"event", "requester"})
    @Query("""
            Select r From Request r
            WHERE r.id in(:requestsIds)
            """)
    Optional<List<Request>> findRequestsByIds(@Param("requestsIds") List<Long> requestsIds);

    @EntityGraph(attributePaths = {"event", "requester"})
    @Query("""
            Select r From Request r
            WHERE r.event.id = :eventId
            """)
    List<Request> getRequestByEventId(@Param("eventId") Long eventId);
}