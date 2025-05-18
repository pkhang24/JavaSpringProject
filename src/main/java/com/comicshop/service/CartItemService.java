package com.comicshop.service;

import com.comicshop.model.CartItem;
import com.comicshop.repository.CartItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;

    public CartItemService(CartItemRepository cartItemRepository) {
        this.cartItemRepository = cartItemRepository;
    }

    public List<CartItem> findByProductId(Long productId) {
        return cartItemRepository.findByProductProductId(productId);
    }

    @Transactional
    public void deleteByProductId(Long productId) {
        cartItemRepository.deleteByProductProductId(productId);
    }
}