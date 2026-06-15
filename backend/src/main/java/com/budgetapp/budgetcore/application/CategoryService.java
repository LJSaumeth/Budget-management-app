package com.budgetapp.budgetcore.application;

import com.budgetapp.budgetcore.domain.Category;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryRequest;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.CategoryRepository;
import com.budgetapp.shared.exception.ConflictException;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Category '" + request.name() + "' already exists");
        }
        Category category = new Category(request.name());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (!category.getName().equalsIgnoreCase(request.name())
                && categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Category '" + request.name() + "' already exists");
        }
        category.setName(request.name());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        if (!category.getExpenses().isEmpty()) {
            throw new ConflictException("Category is in use and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
