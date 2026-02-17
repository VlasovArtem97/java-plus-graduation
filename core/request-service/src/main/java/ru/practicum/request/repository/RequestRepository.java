package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.request.model.Request;

import java.util.Collection;
import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {


    boolean existsByRequesterIdInAndEventIdIn(Collection<Long> userId, Collection<Long> eventIds);

    List<Request> findAllByRequesterId(Long requesterId);

    Request findByIdAndRequesterId(Long requestId, Long requesterId);

    @Query("""
            Select r From Request r
            WHERE r.id in(:requestsIds)
            """)
    List<Request> findRequestsByIds(@Param("requestsIds") List<Long> requestsIds);

    @Query("""
            Select r From Request r
            WHERE r.eventId = :eventId
            """)
    List<Request> getRequestByEventId(@Param("eventId") Long eventId);
}