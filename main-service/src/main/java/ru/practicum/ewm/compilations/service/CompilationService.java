package ru.practicum.ewm.compilations.service;

import ru.practicum.ewm.compilations.dto.CompilationDTO;
import ru.practicum.ewm.compilations.dto.RequestToCreateNewCompilationDTO;
import ru.practicum.ewm.compilations.dto.UpdateCompilationDTO;

import java.util.List;

public interface CompilationService {

    CompilationDTO addCompilation(RequestToCreateNewCompilationDTO newCompilationDTO);

    CompilationDTO updateCompilation(Long compId, UpdateCompilationDTO updateCompilationDTO);

    List<CompilationDTO> getCompilationsList(Boolean pinned, Integer from, Integer size);

    CompilationDTO getCompilationById(Long compId);

    void removeCompilation(Long compId);

}