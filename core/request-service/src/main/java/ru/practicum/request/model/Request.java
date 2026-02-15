package ru.practicum.request.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString
@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime created;

//    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Long eventId;

//    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus;
}
