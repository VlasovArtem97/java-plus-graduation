package stats.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import stats.service.StatsService;
import statsdto.HitDto;
import statsdto.StatDto;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {

    private static final String PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern(PATTERN);

    private final StatsService service;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody HitDto dto, HttpServletRequest req) {
        if (dto.getIp() == null || dto.getIp().isBlank()) {
            dto.setIp(req.getRemoteAddr());
        }
        service.saveHit(dto);
    }

    @GetMapping("/stats")
    public List<StatDto> stats(@RequestParam String start,
                               @RequestParam String end,
                               @RequestParam(required = false) List<String> uris,
                               @RequestParam(defaultValue = "false") boolean unique) {
        // на случай, если клиент прислал закодированные строки
        String decStart = URLDecoder.decode(start, StandardCharsets.UTF_8);
        String decEnd = URLDecoder.decode(end, StandardCharsets.UTF_8);

        LocalDateTime from = LocalDateTime.parse(decStart, FMT);
        LocalDateTime to = LocalDateTime.parse(decEnd, FMT);

        if (to.isBefore(from)) throw new BadRequestException("end must be after start");
        return service.stats(from, to, uris, unique);
    }
}

