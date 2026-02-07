package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repo.CategoryRepository;
import ru.practicum.ewm.error.ConflictException;
import ru.practicum.ewm.error.NotFoundException;
import ru.practicum.ewm.util.PageRequestUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository repo;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto dto) {
        if (repo.existsByNameIgnoreCase(dto.getName()))
            throw new ConflictException("Category name already exists: " + dto.getName());
        Category saved = repo.save(categoryMapper.toEntity(dto));
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(long id, CategoryDto dto) {
        Category cat = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
        if (repo.existsByNameIgnoreCaseAndIdNot(dto.getName(), id))
            throw new ConflictException("Category name already exists: " + dto.getName());
        cat.setName(dto.getName());
        return categoryMapper.toDto(repo.save(cat));
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        if (!repo.existsById(id)) throw new NotFoundException("Category with id=" + id + " was not found");
        // В будущем здесь проверим привязанные события и вернём 409
        repo.deleteById(id);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        var pr = PageRequestUtil.of(from, size, Sort.by("id").ascending());
        return repo.findAll(pr).map(categoryMapper::toDto).getContent();
    }

    //Поиск категории
    @Override
    public CategoryDto getCategory(long id) {
        return repo.findById(id).map(categoryMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
    }
}

