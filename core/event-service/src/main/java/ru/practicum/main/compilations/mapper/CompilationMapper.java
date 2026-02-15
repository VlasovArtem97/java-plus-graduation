package ru.practicum.main.compilations.mapper;

import org.mapstruct.*;
import ru.practicum.interaction.dto.compilatins.CompilationDTO;
import ru.practicum.interaction.dto.compilatins.RequestToCreateNewCompilationDTO;
import ru.practicum.interaction.dto.compilatins.UpdateCompilationDTO;
import ru.practicum.main.compilations.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(RequestToCreateNewCompilationDTO newCompilationDTO);

    CompilationDTO toCompilationDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", ignore = true)
    void updateCompilation(UpdateCompilationDTO updateCompilationDTO, @MappingTarget Compilation compilation);


}