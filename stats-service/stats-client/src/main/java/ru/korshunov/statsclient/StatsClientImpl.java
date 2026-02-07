package ru.korshunov.statsclient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import statsdto.HitDto;
import statsdto.StatDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClientImpl implements StatsClient {

    private final RestClient restClient;
    private static final String PATH_HIT = "/hit";
    private static final String PATH_STATS = "/stats";
    //добавил
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(@Value("${stats-client}") String statsClientURL, RestClient.Builder restClient) {
        this.restClient = restClient
                .baseUrl(statsClientURL)
                .build();
    }

    @Override
    public void addHit(@Valid HitDto hitDto) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_HIT)
                .build()
                .toUriString();

        post(uri, hitDto);
    }

    private ResponseEntity<Void> post(String uri, Object body) {
        return restClient
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start)
                .queryParam("end", end)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    private List<StatDto> get(String uri) {
        return restClient
                .get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<StatDto>>() {
                });
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("uris", uris)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, Boolean unique) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start)
                .queryParam("end", end)
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }

    @Override
    public List<StatDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        String uri = UriComponentsBuilder
                .fromPath(PATH_STATS)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("uris", uris)
                .queryParam("unique", unique)
                .build()
                .encode()
                .toUriString();

        return get(uri);
    }
}