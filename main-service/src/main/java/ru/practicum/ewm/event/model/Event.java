package ru.practicum.ewm.event.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Formula;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.compilations.model.Compilation;
import ru.practicum.ewm.event.status.StateEvent;
import ru.practicum.ewm.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Builder
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long id;

    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "confirmed_requests")
    private Long confirmedRequests;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "description", length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User initiator;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "paid", nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit")
    private Long participantLimit;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "state", nullable = false)
    private StateEvent state;

    @Column(name = "title", nullable = false)
    private String title;

    @Transient
    private Long views;

    @Formula("(select coalesce(sum(er.value),0) from event_ratings er where er.event_id = event_id)")
    private Long rating;

    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(mappedBy = "events", fetch = FetchType.LAZY)
    private List<Compilation> compilations;
}
