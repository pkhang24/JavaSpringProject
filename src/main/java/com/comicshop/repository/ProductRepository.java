package com.comicshop.repository;

import com.comicshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryCategoryId(Long categoryId);
    List<Product> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}