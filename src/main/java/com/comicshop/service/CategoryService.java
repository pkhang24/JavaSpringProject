package com.comicshop.service;

import com.comicshop.model.Category;
import com.comicshop.model.Product;
import com.comicshop.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CategoryService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category saveCategory(Category category) {
        if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        if (category.getCategoryId() == null) {
            category.setCreatedAt(LocalDateTime.now());
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        List<Product> products = productService.findByCategoryId(id);
        if (!products.isEmpty()) {
            throw new IllegalStateException("Cannot delete category because it is associated with one or more products.");
        }
        categoryRepository.deleteById(id);
    }
}