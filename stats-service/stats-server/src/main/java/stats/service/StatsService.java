package stats.service;

import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface StatsService {
    void saveHit(HitDto dto);

    List<StatDto> stats(LocalDateTime start, LocalDateTime end, Collection<String> uris, boolean unique);
}
