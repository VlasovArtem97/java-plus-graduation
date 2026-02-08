package ru.practicum.ewm.compilations.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.compilations.dto.CompilationDTO;
import ru.practicum.ewm.compilations.dto.RequestToCreateNewCompilationDTO;
import ru.practicum.ewm.compilations.dto.UpdateCompilationDTO;
import ru.practicum.ewm.compilations.service.CompilationService;

import java.util.Objects;

@RestController
@RequestMapping("/admin/compilations")
@Validated
public class AdminCompilationController {

    private final CompilationService compilationService;

    @Autowired
    public AdminCompilationController(CompilationService compilationService) {
        this.compilationService = compilationService;
    }

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDTO addCompilation(@Valid @RequestBody RequestToCreateNewCompilationDTO newCompilationDTO) {
        return compilationService.addCompilation(newCompilationDTO);
    }

    @PatchMapping("/{compId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CompilationDTO updateCompilation(
            @Positive @PathVariable Long compId,
            @Valid @RequestBody(required = false) UpdateCompilationDTO updateCompilationDTO) {
        return compilationService.updateCompilation(compId,
                Objects.requireNonNullElseGet(updateCompilationDTO, UpdateCompilationDTO::new));
    }

    @DeleteMapping("/{compId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void removeCompilation(@Positive @PathVariable Long compId) {
        compilationService.removeCompilation(compId);
    }
}