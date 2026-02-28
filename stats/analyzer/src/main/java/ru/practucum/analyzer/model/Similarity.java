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
                columnNames = {"event_a", "event_b"}
        )
})
@ToString
public class Similarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "timestamp", nullable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant instant;
}
