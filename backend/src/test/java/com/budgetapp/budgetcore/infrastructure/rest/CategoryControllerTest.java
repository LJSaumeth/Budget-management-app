package com.budgetapp.budgetcore.infrastructure.rest;

import com.budgetapp.budgetcore.application.CategoryService;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryRequest;
import com.budgetapp.budgetcore.infrastructure.dto.CategoryResponse;
import com.budgetapp.shared.exception.ConflictException;
import com.budgetapp.shared.exception.GlobalExceptionHandler;
import com.budgetapp.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldReturn201_whenCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Investments");
        CategoryResponse response = new CategoryResponse(10L, "Investments");

        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Investments"));
    }

    @Test
    void shouldReturn400_whenNameIsBlank() throws Exception {
        CategoryRequest request = new CategoryRequest("");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn409_whenDuplicateName() throws Exception {
        CategoryRequest request = new CategoryRequest("Food");
        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new ConflictException("Category 'Food' already exists"));

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn200_whenGetAllCategories() throws Exception {
        CategoryResponse cat1 = new CategoryResponse(1L, "Food");
        CategoryResponse cat2 = new CategoryResponse(2L, "Transport");
        when(categoryService.getAll()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[1].name").value("Transport"));
    }

    @Test
    void shouldReturn200_whenUpdateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Groceries");
        CategoryResponse response = new CategoryResponse(1L, "Groceries");

        when(categoryService.update(eq(1L), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Groceries"));
    }

    @Test
    void shouldReturn204_whenDeleteCategory() throws Exception {
        doNothing().when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn409_whenDeleteCategoryInUse() throws Exception {
        doThrow(new ConflictException("Category is in use and cannot be deleted"))
                .when(categoryService).delete(1L);

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404_whenCategoryNotFound() throws Exception {
        when(categoryService.update(eq(99L), any(CategoryRequest.class)))
                .thenThrow(new ResourceNotFoundException("Category", 99L));

        CategoryRequest request = new CategoryRequest("Test");

        mockMvc.perform(put("/api/categories/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
}
