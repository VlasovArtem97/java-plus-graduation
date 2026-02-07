package stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import stats.model.Hit;
import stats.repo.HitRepository;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private final HitRepository repo;

    @Override
    @Transactional
    public void saveHit(HitDto dto) {
        Hit hit = Hit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp() == null ? LocalDateTime.now() : dto.getTimestamp())
                .build();
        repo.save(hit);
    }

    @Override
    public List<StatDto> stats(LocalDateTime start, LocalDateTime end, Collection<String> uris, boolean unique) {
        boolean empty = (uris == null || uris.isEmpty());
        var rows = unique
                ? repo.findStatsUnique(start, end, uris, empty)
                : repo.findStats(start, end, uris, empty);
        return rows.stream()
                .map(r -> StatDto.builder().app(r.getApp()).uri(r.getUri()).hits(r.getHits()).build())
                .toList();
    }
}
