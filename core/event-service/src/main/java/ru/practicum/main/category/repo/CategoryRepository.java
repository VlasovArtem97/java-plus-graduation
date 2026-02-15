package ru.practicum.main.category.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.main.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
