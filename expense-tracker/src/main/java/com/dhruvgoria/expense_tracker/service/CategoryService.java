package com.dhruvgoria.expense_tracker.service;

import com.dhruvgoria.expense_tracker.entity.Category;
import com.dhruvgoria.expense_tracker.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        // Business rule: Check if category already exists before saving
        Optional<Category> existing = categoryRepository.findByName(category.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Category already exists!");
        }
        return categoryRepository.save(category);
    }
}