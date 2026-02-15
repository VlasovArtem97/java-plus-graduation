package ru.practicum.main.compilations.service;

import ru.practicum.interaction.dto.compilatins.CompilationDTO;
import ru.practicum.interaction.dto.compilatins.RequestToCreateNewCompilationDTO;
import ru.practicum.interaction.dto.compilatins.UpdateCompilationDTO;

import java.util.List;

public interface CompilationService {

    CompilationDTO addCompilation(RequestToCreateNewCompilationDTO newCompilationDTO);

    CompilationDTO updateCompilation(Long compId, UpdateCompilationDTO updateCompilationDTO);

    List<CompilationDTO> getCompilationsList(Boolean pinned, Integer from, Integer size);

    CompilationDTO getCompilationById(Long compId);

    void removeCompilation(Long compId);

}