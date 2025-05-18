package com.comicshop.controller;

import com.comicshop.model.Category;
import com.comicshop.model.Product;
import com.comicshop.model.User;
import com.comicshop.service.CategoryService;
import com.comicshop.service.ProductService;
import com.comicshop.service.RoleService;
import com.comicshop.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final RoleService roleService;

    public AdminController(ProductService productService, CategoryService categoryService, UserService userService, RoleService roleService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String adminDashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        return "admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String adminDashboardExplicit(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("username", username);
        return "admin/dashboard";
    }

    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "admin/product-manager";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products")
    public String saveProduct(@ModelAttribute Product product, @RequestParam("coverImage") MultipartFile coverImage) {
        if (!coverImage.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/images/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String fileName = UUID.randomUUID().toString() + "_" + coverImage.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, coverImage.getBytes());
                product.setCoverImageUrl("/images/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product ID"));
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/product-form";
    }

    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute Product product, @RequestParam("coverImage") MultipartFile coverImage) {
        if (!coverImage.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/images/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }
                String fileName = UUID.randomUUID().toString() + "_" + coverImage.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, coverImage.getBytes());
                product.setCoverImageUrl("/images/" + fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        productService.saveProduct(product); // Lưu sản phẩm đã chỉnh sửa
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, Model model) {
        try {
            Product product = productService.getProductById(id).orElse(null);
            if (product == null) {
                model.addAttribute("errorMessage", "Product not found");
                return "error";
            }
            productService.deleteProduct(id);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting product: " + e.getMessage());
            return "error";
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/categories")
    public String manageCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/category-manager";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        try {
            model.addAttribute("category", new Category());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/category-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading category form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute Category category) {
        try {
            categoryService.saveCategory(category);
            return "redirect:/admin/categories";
        } catch (Exception e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
        }
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.getCategoryById(id).orElse(null);
            if (category == null) {
                model.addAttribute("errorMessage", "Category not found");
                return "error";
            }
            model.addAttribute("category", category);
            model.addAttribute("categories", categoryService.getAllCategories());
            return "admin/category-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading category form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/categories/update")
    public String updateCategory(@ModelAttribute Category category, @RequestParam(required = false) Long parentCategoryId) {
        try {
            // Nạp lại category từ database để đảm bảo trạng thái managed
            Category existingCategory = categoryService.getCategoryById(category.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));

            // Cập nhật các trường từ form
            existingCategory.setCategoryName(category.getCategoryName());
            existingCategory.setDescription(category.getDescription());

            // Xử lý parentCategory
            if (parentCategoryId != null) {
                Category parentCategory = categoryService.getCategoryById(parentCategoryId).orElse(null);
                if (parentCategory != null && !parentCategory.getCategoryId().equals(category.getCategoryId())) {
                    existingCategory.setParentCategory(parentCategory);
                } else {
                    existingCategory.setParentCategory(null); // Tránh vòng lặp
                }
            } else {
                existingCategory.setParentCategory(null);
            }

            categoryService.saveCategory(existingCategory);
            return "redirect:/admin/categories";
        } catch (Exception e) {
            return "redirect:/admin/categories?error=" + e.getMessage();
        }
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Model model) {
        try {
            Category category = categoryService.getCategoryById(id).orElse(null);
            if (category == null) {
                model.addAttribute("errorMessage", "Category not found");
                return "error";
            }
            categoryService.deleteCategory(id);
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting category: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            model.addAttribute("users", userService.getAllUsers());
            return "admin/user-manager";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading users: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                model.addAttribute("errorMessage", "User not found");
                return "error";
            }
            model.addAttribute("user", user);
            model.addAttribute("roles", roleService.getAllRoles()); // Thêm danh sách roles
            return "admin/user-form";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading user form: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/users/update")
    public String updateUser(@ModelAttribute User user) {
        try {
            // Nạp user hiện tại từ database
            User existingUser = userService.getUserById(user.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            // Cập nhật các trường từ form
            existingUser.setUsername(user.getUsername());
            existingUser.setEmail(user.getEmail());
            existingUser.setRole(user.getRole());
            existingUser.setIsActive(user.getIsActive());

            // Chỉ cập nhật password hash nếu không để trống
            if (user.getPasswordHash() != null && !user.getPasswordHash().trim().isEmpty()) {
                existingUser.setPasswordHash(user.getPasswordHash()); // Hoặc setPassword nếu dùng cột password
            }

            userService.saveUser(existingUser);
            return "redirect:/admin/users";
        } catch (Exception e) {
            return "redirect:/admin/users?error=" + e.getMessage();
        }
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id).orElse(null);
            if (user == null) {
                model.addAttribute("errorMessage", "User not found");
                return "error";
            }
            userService.deleteUser(id);
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting user: " + e.getMessage());
            return "error";
        }
    }
}