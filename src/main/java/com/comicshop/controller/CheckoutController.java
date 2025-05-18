package com.comicshop.controller;

import com.comicshop.model.Cart;
import com.comicshop.model.CartItem;
import com.comicshop.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/checkout")
public class CheckoutController {
    private final CartService cartService;

    public CheckoutController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String showCheckoutPage(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        Cart cart = cartService.getCartByUsername(username);
        List<CartItem> cartItems = (cart != null && cart.getCartItems() != null) ? cart.getCartItems() : new ArrayList<>();

        // Tính tổng (nếu cần)
        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal price = (item.getProduct().getDiscountPrice() != null)
                            ? item.getProduct().getDiscountPrice()
                            : (item.getProduct().getPrice() != null ? item.getProduct().getPrice() : BigDecimal.ZERO);
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total.doubleValue());
        return "checkout"; // Trả về template checkout.html
    }

    @PostMapping
    public String processCheckout(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        // Logic xử lý thanh toán (ví dụ: lưu đơn hàng, xóa giỏ hàng)
        cartService.clearCart(username); // Ví dụ
        return "redirect:/order-confirmation"; // Chuyển đến trang xác nhận
    }
}
