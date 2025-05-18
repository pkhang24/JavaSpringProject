package com.comicshop.controller;

import com.comicshop.dto.CartItemDto;
import com.comicshop.model.Cart;
import com.comicshop.model.CartItem;
import com.comicshop.service.CartService;
import com.comicshop.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);
        List<CartItem> cartItems = (cart != null && cart.getCartItems() != null) ? cart.getCartItems() : new ArrayList<>();

        // Tính tổng với BigDecimal
        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal price = (item.getProduct().getDiscountPrice() != null)
                            ? item.getProduct().getDiscountPrice()
                            : (item.getProduct().getPrice() != null ? item.getProduct().getPrice() : BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total.doubleValue()); // Chuyển về double cho Thymeleaf
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@ModelAttribute CartItemDto cartItemDto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        cartService.addToCart(username, cartItemDto.getProductId(), cartItemDto.getQuantity());
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long cartItemId, @RequestParam int quantity, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        cartService.updateCartItem(username, cartItemId, quantity);
        return "redirect:/cart";
    }

    @GetMapping("/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        cartService.removeFromCart(username, cartItemId);
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);

        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            return "redirect:/cart?error=emptyCart";
        }

        // Logic xử lý thanh toán (giả định)
        // Ví dụ: Lưu đơn hàng vào database (cần OrderService)
        // cartService.processOrder(username, cart);

        // Xóa giỏ hàng sau khi thanh toán
        cartService.clearCart(username);

        // Chuyển hướng đến trang xác nhận đơn hàng
        return "redirect:/order-confirmation";
    }
}