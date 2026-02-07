package ru.practicum.ewm.category.controller.Public;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Validated
public class PublicCategoryController {
    private final CategoryService service;

    @GetMapping
    public List<CategoryDto> getCategories(@PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                           @Positive @RequestParam(defaultValue = "10") int size) {
        return service.getCategories(from, size);
    }

    @GetMapping("/{catId}")
    public CategoryDto getCategory(@PathVariable long catId) {
        return service.getCategory(catId);
    }
}
