package com.ecommerce.backend.config;

import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.UserRole;
import com.ecommerce.backend.model.Category;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.existsByEmail("admin@ecommerce.local")) {
                return;
            }

            User admin = User.builder()
                    .username("admin")
                    .email("admin@ecommerce.local")
                    .password(passwordEncoder.encode("Admin1234!"))
                    .role(UserRole.ROLE_ADMIN)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
        };
    }

    @Bean
    CommandLineRunner seedDemoProducts(CategoryRepository categoryRepository, ProductRepository productRepository) {
        return args -> {
            if (!categoryRepository.findAll().isEmpty() || !productRepository.findAll().isEmpty()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            Category electronics = categoryRepository.save(Category.builder()
                    .name("3C 電子")
                    .description("手機、筆電與周邊配件")
                    .createdAt(now)
                    .updatedAt(now)
                    .build());

            Category homeLife = categoryRepository.save(Category.builder()
                    .name("居家生活")
                    .description("日常生活與居家用品")
                    .createdAt(now)
                    .updatedAt(now)
                    .build());

            productRepository.save(Product.builder()
                    .name("無線藍牙耳機")
                    .description("支援降噪與快速充電，通勤與運動都適合。")
                    .price(new BigDecimal("1990"))
                    .stock(45)
                    .imageUrl("https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=900&q=80")
                    .active(true)
                    .category(electronics)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());

            productRepository.save(Product.builder()
                    .name("人體工學辦公椅")
                    .description("可調式頭枕與腰靠，久坐更舒適。")
                    .price(new BigDecimal("4990"))
                    .stock(18)
                    .imageUrl("https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=900&q=80")
                    .active(true)
                    .category(homeLife)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        };
    }
}
