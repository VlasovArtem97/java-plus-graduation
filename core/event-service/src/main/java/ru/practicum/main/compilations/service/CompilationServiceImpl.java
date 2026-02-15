package ru.practicum.main.compilations.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.interaction.dto.compilatins.CompilationDTO;
import ru.practicum.interaction.dto.compilatins.RequestToCreateNewCompilationDTO;
import ru.practicum.interaction.dto.compilatins.UpdateCompilationDTO;
import ru.practicum.interaction.error.NotFoundException;
import ru.practicum.interaction.utill.PageRequestUtil;
import ru.practicum.main.compilations.mapper.CompilationMapper;
import ru.practicum.main.compilations.model.Compilation;
import ru.practicum.main.compilations.repository.CompilationRepository;
import ru.practicum.main.event.mapper.EventMapper;
import ru.practicum.main.event.model.Event;
import ru.practicum.main.event.service.EventService;

import java.util.ArrayList;
import java.util.List;

@Transactional(readOnly = true)
@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventService eventService;
    private final EventMapper eventMapper;
    private final CompilationMapper compilationMapper;

    @Autowired
    public CompilationServiceImpl(CompilationRepository compilationRepository, EventService eventService, EventMapper eventMapper, CompilationMapper compilationMapper) {
        this.compilationRepository = compilationRepository;
        this.eventService = eventService;
        this.eventMapper = eventMapper;
        this.compilationMapper = compilationMapper;
    }

    @Transactional
    @Override
    public CompilationDTO addCompilation(RequestToCreateNewCompilationDTO newCompilationDTO) {
        if (newCompilationDTO.getPinned() == null) {
            newCompilationDTO.setPinned(false);
        }
        List<Event> events = new ArrayList<>();
        if (newCompilationDTO.getEvents() != null && !newCompilationDTO.getEvents().isEmpty()) {
            events = eventService.findEventsByids(newCompilationDTO.getEvents());
        }
        Compilation compilation = compilationMapper.toCompilation(newCompilationDTO);
        compilation.setEvents(events);
        return compilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    @Transactional
    @Override
    public CompilationDTO updateCompilation(Long compId, UpdateCompilationDTO updateCompilationDTO) {
        Compilation foundCompilation = findCompilationById(compId);
        if (updateCompilationDTO.getEvents() != null && !updateCompilationDTO.getEvents().isEmpty()) {
            foundCompilation.setEvents(eventService.findEventsByids(updateCompilationDTO.getEvents()));
        }
        compilationMapper.updateCompilation(updateCompilationDTO, foundCompilation);
        return compilationMapper.toCompilationDto(foundCompilation);
    }

    private Compilation findCompilationById(long compId) {
        return compilationRepository.findById(compId)
                .orElseThrow(() ->
                        new NotFoundException(String.format("Подборка с указанным id - %d не найдена.", compId)));
    }

    @Override
    public List<CompilationDTO> getCompilationsList(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        return compilationRepository.findCompilationByPinned(pinned, pageable).stream()
                .map(compilationMapper::toCompilationDto)
                .toList();
    }

    @Override
    public CompilationDTO getCompilationById(Long compId) {
        return compilationMapper.toCompilationDto(findCompilationById(compId));
    }

    @Transactional
    @Override
    public void removeCompilation(Long compId) {
        findCompilationById(compId);
        compilationRepository.deleteById(compId);
    }
}