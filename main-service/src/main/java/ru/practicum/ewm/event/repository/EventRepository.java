package ru.practicum.ewm.event.repository;

import com.querydsl.core.BooleanBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.dto.EventAdminParamDto;
import ru.practicum.ewm.event.dto.EventPublicParamsDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.QEvent;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.util.DateTimeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query("""
            Select e From Event e
            Where e.initiator.id = :userId
            """)
    Page<Event> findEventByUserId(@Param("userId") Long userId, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query("""
            Select e From Event e
            Where e.id = :eventId
            AND e.initiator.id = :userId
            """)
    Optional<Event> findEventByUserIdAndEventId(@Param("eventId") Long eventId, @Param("userId") Long userId);

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query("""
            Select e From Event e
            Where e.id = :eventId
            """)
    Optional<Event> findEventById(@Param("eventId") Long eventId);

    @EntityGraph(attributePaths = {"category", "initiator", "location"})
    @Query("""
            Select e From Event e
            Where e.id in(:eventsId)
            """)
    List<Event> findEventsByIds(@Param("eventsId") List<Long> eventsId);


    interface PredicatesForParamAdmin {
        static BooleanBuilder build(EventAdminParamDto eventParamDto) {
            QEvent qEvent = QEvent.event;
            BooleanBuilder booleanBuilder = new BooleanBuilder();

            if (eventParamDto.getUsers() != null && !eventParamDto.getUsers().isEmpty()) {
                booleanBuilder.and(qEvent.initiator.id.in(eventParamDto.getUsers()));
            }
            if (eventParamDto.getState() != null && !eventParamDto.getState().isEmpty()) {
                booleanBuilder.and(qEvent.state.in(
                        eventParamDto.getState().stream()
                                .map(StateEvent::valueOf)
                                .toList()
                ));
            }
            if (eventParamDto.getCategories() != null && !eventParamDto.getCategories().isEmpty()) {
                booleanBuilder.and(qEvent.category.id.in(eventParamDto.getCategories()));
            }
            if (eventParamDto.getRangeStart() != null) {
                booleanBuilder.and(qEvent.eventDate.goe(eventParamDto.getRangeStart()));
            }
            if (eventParamDto.getRangeEnd() != null) {
                booleanBuilder.and(qEvent.eventDate.loe(eventParamDto.getRangeEnd()));
            }
            return booleanBuilder;
        }
    }

    interface PredicatesForParamPublic {
        static BooleanBuilder build(EventPublicParamsDto eventPublicParamsDto) {
            QEvent qEvent = QEvent.event;
            BooleanBuilder booleanBuilder = new BooleanBuilder();

            booleanBuilder.and(qEvent.state.eq(StateEvent.PUBLISHED));
            if (eventPublicParamsDto.getText() != null && !eventPublicParamsDto.getText().isBlank()) {
                booleanBuilder.and(qEvent.annotation.containsIgnoreCase(eventPublicParamsDto.getText())
                        .or(qEvent.description.containsIgnoreCase(eventPublicParamsDto.getText()))
                );
            }
            if (eventPublicParamsDto.getCategories() != null && !eventPublicParamsDto.getCategories().isEmpty()) {
                booleanBuilder.and(qEvent.category.id.in(eventPublicParamsDto.getCategories()));
            }
            if (eventPublicParamsDto.getPaid() != null) {
                booleanBuilder.and(qEvent.paid.eq(eventPublicParamsDto.getPaid()));
            }
            if (eventPublicParamsDto.getRangeStart() != null && eventPublicParamsDto.getRangeEnd() != null) {
                if (DateTimeUtil.isValidStartAndEnd(eventPublicParamsDto.getRangeStart(), eventPublicParamsDto.getRangeEnd())) {
                    booleanBuilder.and(qEvent.eventDate.goe(eventPublicParamsDto.getRangeStart()))
                            .and(qEvent.eventDate.loe(eventPublicParamsDto.getRangeEnd()));
                }
            } else {
                booleanBuilder.and(qEvent.eventDate.goe(LocalDateTime.now()));
            }
            if (eventPublicParamsDto.getOnlyAvailable()) {
                booleanBuilder.and(qEvent.participantLimit.gt(qEvent.confirmedRequests));
            } else {
                booleanBuilder.and(qEvent.participantLimit.goe(qEvent.confirmedRequests));
            }
            return booleanBuilder;
        }
    }
}
