package com.comicshop.service;

import com.comicshop.model.Cart;
import com.comicshop.model.CartItem;
import com.comicshop.model.Product;
import com.comicshop.model.User;
import com.comicshop.repository.CartItemRepository;
import com.comicshop.repository.CartRepository;
import com.comicshop.repository.ProductRepository;
import com.comicshop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductRepository productRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Cart getCartByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Optional<Cart> cart = cartRepository.findByUserUsername(username);
        if (cart.isPresent()) {
            return cart.get();
        }

        Cart newCart = new Cart();
        newCart.setUser(user.get());
        newCart.setCreatedAt(LocalDateTime.now());
        newCart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(newCart);
    }

    @Transactional
    public void addToCart(String username, Long productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getCartByUsername(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getProductId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setAddedAt(LocalDateTime.now());
            cart.addCartItem(cartItem);
            cartItemRepository.save(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void updateCartItem(String username, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = getCartByUsername(username);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void removeFromCart(String username, Long cartItemId) {
        Cart cart = getCartByUsername(username);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!cart.getCartItems().contains(cartItem)) {
            throw new IllegalArgumentException("Cart item does not belong to this cart");
        }

        cart.removeCartItem(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String username) {
        Cart cart = getCartByUsername(username);
        if (cart != null) {
            cart.getCartItems().clear(); // Xóa tất cả các mục trong giỏ hàng
            cartRepository.save(cart);   // Lưu lại thay đổi
        }
    }
}