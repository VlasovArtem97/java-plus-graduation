package ru.practicum.ewm.compilations.mapper;

import org.mapstruct.*;
import ru.practicum.ewm.compilations.dto.CompilationDTO;
import ru.practicum.ewm.compilations.dto.RequestToCreateNewCompilationDTO;
import ru.practicum.ewm.compilations.dto.UpdateCompilationDTO;
import ru.practicum.ewm.compilations.model.Compilation;

@Mapper(componentModel = "spring")
public interface CompilationMapper {

    @Mapping(target = "events", ignore = true)
    Compilation toCompilation(RequestToCreateNewCompilationDTO newCompilationDTO);

    CompilationDTO toCompilationDto(Compilation compilation);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "events", ignore = true)
    void updateCompilation(UpdateCompilationDTO updateCompilationDTO, @MappingTarget Compilation compilation);


}