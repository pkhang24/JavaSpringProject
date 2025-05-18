package com.comicshop.controller;

import com.comicshop.model.Product;
import com.comicshop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ProductService productService;

    public SearchController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public String search(@RequestParam("query") String query, Model model) {
        // Tìm kiếm sản phẩm theo query
        List<Product> searchResults = productService.searchProducts(query);
        model.addAttribute("products", searchResults);
        model.addAttribute("query", query); // Để hiển thị query trên trang kết quả
        return "products"; // Trả về trang products.html để hiển thị kết quả
    }
}