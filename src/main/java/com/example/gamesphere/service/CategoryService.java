package com.example.gamesphere.service;

import com.example.gamesphere.dto.request.CategoryCreateRequest;
import com.example.gamesphere.dto.response.CategoryResponse;
import com.example.gamesphere.entity.Category;
import com.example.gamesphere.exception.BusinessException;
import com.example.gamesphere.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        String normalizedName = request.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("Category already exists: " + normalizedName);
        }

        Category category = Category.builder()
                .name(normalizedName)
                .description(request.getDescription())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")).stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription());
    }
}
