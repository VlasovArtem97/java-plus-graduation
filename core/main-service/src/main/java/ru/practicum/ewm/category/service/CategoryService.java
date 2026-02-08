package ru.practicum.ewm.category.service;


import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto dto);

    CategoryDto updateCategory(long id, CategoryDto dto);

    void deleteCategory(long id);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategory(long id);
}
