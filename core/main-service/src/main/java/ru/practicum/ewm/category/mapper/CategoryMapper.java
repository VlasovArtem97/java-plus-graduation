package ru.practicum.ewm.category.mapper;

import org.mapstruct.Mapper;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(NewCategoryDto dto);

    CategoryDto toDto(Category e);

    Category toCategory(CategoryDto categoryDto);
}