package ru.practicum.rating.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_ratings",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_user", columnNames = {"event_id", "user_id"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "event_id", nullable = false)
    private Long event;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Column(name = "user_id", nullable = false)
    private Long user;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction", nullable = false, length = 16)
    private ReactionType reaction;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
