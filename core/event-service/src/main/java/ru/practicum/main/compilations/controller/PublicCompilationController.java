package ru.practicum.main.compilations.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.interaction.dto.compilatins.CompilationDTO;
import ru.practicum.main.compilations.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@Validated
public class PublicCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public PublicCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @GetMapping
    @ResponseStatus(value = HttpStatus.OK)
    public List<CompilationDTO> getCompilationsList(
            @RequestParam(defaultValue = "false") Boolean pinned,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {
        return compilationService.getCompilationsList(pinned, from, size);
    }

    @GetMapping("/{compId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationDTO getCompilationById(@PathVariable @NotNull @Positive Long compId) {
        return compilationService.getCompilationById(compId);
    }
}