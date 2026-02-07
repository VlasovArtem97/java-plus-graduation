package ru.korshunov.statsclient;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.MaxAttemptsRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import statsdto.HitDto;
import statsdto.StatDto;
import statsdto.exception.StatsServerUnavailable;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatsClientImpl implements StatsClient {

    //Добавил Discovery и retry
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final String statServerId;

    private final RestClient restClient;
    private static final String PATH_HIT = "/hit";
    private static final String PATH_STATS = "/stats";
    //добавил
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public StatsClientImpl(DiscoveryClient discoveryClient,
                           @Value("${discovery.services.stats-service-id}") String statServerId,
                           RestClient.Builder restClient) {
        this.discoveryClient = discoveryClient;
        this.statServerId = statServerId;

        //Настройка повторного подключения к stat-service
        this.retryTemplate = new RetryTemplate();

        FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        fixedBackOffPolicy.setBackOffPeriod(3000L);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

        MaxAttemptsRetryPolicy retryPolicy = new MaxAttemptsRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        this.restClient = restClient.build();
//        this.restClient = restClient
//                .baseUrl(makeUri().toString())
//                .build();
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
//                .uri(uri)
                .uri(makeUri() + uri)
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
//                .uri(uri)
                .uri(makeUri() + uri)
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

    private URI makeUri() {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort());
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statServerId)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statServerId,
                    exception
            );
        }
    }
}