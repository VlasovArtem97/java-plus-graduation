package ru.practicum.main.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.main.category.model.Category;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(NewCategoryDto dto);

    CategoryDto toDto(Category e);

    Category toCategory(CategoryDto categoryDto);
}