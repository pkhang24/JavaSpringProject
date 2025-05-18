package com.comicshop.service;

import com.comicshop.model.Product;
import com.comicshop.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CartItemService cartItemService;

    public ProductService(ProductRepository productRepository, CartItemService cartItemService) {
        this.productRepository = productRepository;
        this.cartItemService = cartItemService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id); // Trả về Optional trực tiếp
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Xóa các cart_items liên quan trước
        cartItemService.deleteByProductId(id);
        // Sau đó xóa sản phẩm
        productRepository.deleteById(id);
    }

    public List<Product> findByCategoryId(Long categoryId) {
        return productRepository.findByCategoryCategoryId(categoryId);
    }

    // Thêm phương thức tìm kiếm
    public List<Product> searchProducts(String query) {
        if (query == null || query.trim().isEmpty()) {
            return productRepository.findAll(); // Nếu query rỗng, trả về tất cả sản phẩm
        }
        // Tìm kiếm theo title hoặc author, không phân biệt hoa thường
        return productRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

}