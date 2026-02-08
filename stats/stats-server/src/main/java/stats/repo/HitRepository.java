package stats.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import stats.model.Hit;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    interface StatRow {
        String getApp();

        String getUri();

        Long getHits();
    }

    @Query("""
               select h.app as app, h.uri as uri, count(h.id) as hits
               from Hit h
               where h.timestamp between :start and :end
                 and (:urisEmpty = true or h.uri in :uris)
               group by h.app, h.uri
               order by count(h.id) desc
            """)
    List<StatRow> findStats(@Param("start") LocalDateTime start,
                            @Param("end") LocalDateTime end,
                            @Param("uris") Collection<String> uris,
                            @Param("urisEmpty") boolean urisEmpty);

    @Query("""
               select h.app as app, h.uri as uri, count(distinct h.ip) as hits
               from Hit h
               where h.timestamp between :start and :end
                 and (:urisEmpty = true or h.uri in :uris)
               group by h.app, h.uri
               order by count(distinct h.ip) desc
            """)
    List<StatRow> findStatsUnique(@Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  @Param("uris") Collection<String> uris,
                                  @Param("urisEmpty") boolean urisEmpty);
}
