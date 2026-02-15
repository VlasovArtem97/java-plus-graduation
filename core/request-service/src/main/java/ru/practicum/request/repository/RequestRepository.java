package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.model.Request;
import ru.practicum.request.model.RequestStatus;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {

//    boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    boolean existsByRequesterIdInAndEventIdIn(Collection<Long> userId, Collection<Long> eventIds);

    Long countByEventIdAndRequestStatus(Long eventId, RequestStatus status);

    List<Request> findAllByRequesterId(Long requesterId);

    Request findByIdAndRequesterId(Long requestId, Long requesterId);

//    @EntityGraph(attributePaths = {"event", "requester"})
    @Query("""
            Select r From Request r
            WHERE r.id in(:requestsIds)
            """)
    List<Request> findRequestsByIds(@Param("requestsIds") List<Long> requestsIds);

//    @EntityGraph(attributePaths = {"event", "requester"})
//    @Query("""
//            Select r From Request r
//            WHERE r.event.id = :eventId
//            """)
@Query("""
            Select r From Request r
            WHERE r.eventId = :eventId
            """)
    List<Request> getRequestByEventId(@Param("eventId") Long eventId);
}