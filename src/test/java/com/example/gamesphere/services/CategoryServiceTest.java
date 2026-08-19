package com.example.gamesphere.services;

import com.example.gamesphere.dto.request.CategoryCreateRequest;
import com.example.gamesphere.dto.response.CategoryResponse;
import com.example.gamesphere.entity.Category;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.repository.CategoryRepository;
import com.example.gamesphere.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;
    @InjectMocks CategoryService categoryService;

    @Test
    void createCategoryTrimsNameAndPersistsIt() {
        CategoryCreateRequest request = new CategoryCreateRequest(" RPG ", "Role-playing games");
        when(categoryRepository.existsByNameIgnoreCase("RPG")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(5L);
            return category;
        });

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getName()).isEqualTo("RPG");
    }

    @Test
    void duplicateCategoryNameIsRejectedIgnoringCase() {
        CategoryCreateRequest request = new CategoryCreateRequest("rpg", null);
        when(categoryRepository.existsByNameIgnoreCase("rpg")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }
}
