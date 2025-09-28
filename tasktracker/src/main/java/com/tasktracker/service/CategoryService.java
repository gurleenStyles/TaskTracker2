package com.tasktracker.service;

import com.tasktracker.model.Category;
import com.tasktracker.repo.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all categories
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Get category by ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Create a new category
     */
    public Category createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description);

        return categoryRepository.save(category);
    }

    /**
     * Create a new category from Category object
     */
    public Category createCategory(Category categoryRequest) {
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category category = new Category();
        category.setName(categoryRequest.getName().trim());
        category.setDescription(categoryRequest.getDescription());

        return categoryRepository.save(category);
    }

    /**
     * Update an existing category
     */
    public Optional<Category> updateCategory(Long id, String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(name.trim());
                    category.setDescription(description);
                    return categoryRepository.save(category);
                });
    }

    /**
     * Update an existing category from Category object
     */
    public Optional<Category> updateCategory(Long id, Category categoryRequest) {
        if (categoryRequest.getName() == null || categoryRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }

        return categoryRepository.findById(id)
                .map(category -> {
                    category.setName(categoryRequest.getName().trim());
                    category.setDescription(categoryRequest.getDescription());
                    return categoryRepository.save(category);
                });
    }

    /**
     * Delete a category
     */
    public boolean deleteCategory(Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    categoryRepository.delete(category);
                    return true;
                })
                .orElse(false);
    }

    /**
     * Check if category exists
     */
    public boolean categoryExists(Long id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Check if category exists by name
     */
    public boolean categoryExistsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(name.trim()));
    }
}