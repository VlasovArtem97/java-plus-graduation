package ru.practicum.main.category.service;

import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto dto);

    CategoryDto updateCategory(long id, CategoryDto dto);

    void deleteCategory(long id);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long id);
}
