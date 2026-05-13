package ru.practucum.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
@Table(name = "similarities", uniqueConstraints = {
        @UniqueConstraint(
                name = "unique_event_pair",
                columnNames = {"event1", "event2"}
        )
})
@ToString
public class Similarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event1", nullable = false)
    private Long event1;

    @Column(name = "event2", nullable = false)
    private Long event2;

    @Column(name = "score", nullable = false)
    private Double similarity;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant timestamp;

    public Long getOtherEventId(Long targetEventId) {
        return event1.equals(targetEventId) ? event2 : event1;
    }
}
