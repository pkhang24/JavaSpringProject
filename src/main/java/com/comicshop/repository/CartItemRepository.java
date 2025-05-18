package com.comicshop.repository;

import com.comicshop.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByProductProductId(Long productId);
    void deleteByProductProductId(Long productId);
}