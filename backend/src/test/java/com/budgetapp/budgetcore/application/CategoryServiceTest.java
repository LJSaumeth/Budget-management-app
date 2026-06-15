package com.budgetapp.budgetcore.application;

import com.budgetapp.budgetcore.domain.Category;
import com.budgetapp.budgetcore.domain.Expense;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryRequest;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryResponse;
import com.budgetapp.budgetcore.infrastructure.persistence.CategoryRepository;
import com.budgetapp.shared.exception.ConflictException;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory_whenValidRequest() {
        CategoryRequest request = new CategoryRequest("Investments");
        Category category = new Category("Investments");
        category.setId(10L);

        when(categoryRepository.existsByNameIgnoreCase("Investments")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        CategoryResponse response = categoryService.create(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.name()).isEqualTo("Investments");
    }

    @Test
    void shouldThrow_whenDuplicateName() {
        CategoryRequest request = new CategoryRequest("Food");
        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldReturnAllCategories() {
        Category cat1 = new Category("Food");
        cat1.setId(1L);
        Category cat2 = new Category("Transport");
        cat2.setId(2L);

        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> responses = categoryService.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Food");
    }

    @Test
    void shouldUpdateCategory() {
        Category existing = new Category("Food");
        existing.setId(1L);
        CategoryRequest request = new CategoryRequest("Groceries");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCase("Groceries")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(existing);

        CategoryResponse response = categoryService.update(1L, request);

        assertThat(response.name()).isEqualTo("Groceries");
    }

    @Test
    void shouldThrow_whenUpdateToExistingName() {
        Category existing = new Category("Food");
        existing.setId(1L);
        CategoryRequest request = new CategoryRequest("Transport");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByNameIgnoreCase("Transport")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldDeleteUnusedCategory() {
        Category category = new Category("Investments");
        category.setId(10L);

        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        categoryService.delete(10L);

        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void shouldThrow_whenDeleteCategoryInUse() {
        Category category = new Category("Food");
        category.setId(1L);
        category.getExpenses().add(new Expense());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("in use");
    }

    @Test
    void shouldThrow_whenDeleteNonExistentCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
