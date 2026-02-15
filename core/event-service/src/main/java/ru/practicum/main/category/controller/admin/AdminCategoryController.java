package ru.practicum.main.category.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.category.service.CategoryService;
import ru.practicum.interaction.dto.category.CategoryDto;
import ru.practicum.interaction.dto.category.NewCategoryDto;

@RestController
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class AdminCategoryController {
    private final CategoryService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto addCategory(@Valid @RequestBody NewCategoryDto dto) {
        return service.addCategory(dto);
    }

    @PatchMapping("/{catId}")
    public CategoryDto updateCategory(@PathVariable long catId,
                                      @Valid @RequestBody CategoryDto dto) {
        return service.updateCategory(catId, dto);
    }

    @DeleteMapping("/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        service.deleteCategory(catId);
    }
}

