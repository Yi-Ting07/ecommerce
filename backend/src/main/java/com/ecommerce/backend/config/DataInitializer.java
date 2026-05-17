package com.ecommerce.backend.config;

import com.ecommerce.backend.model.News;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.UserRole;
import com.ecommerce.backend.model.Category;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.CategoryRepository;
import com.ecommerce.backend.repository.NewsRepository;
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

    @Bean
    CommandLineRunner seedNews(NewsRepository newsRepository) {
        return args -> {
            if (!newsRepository.findAll().isEmpty()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            // 置頂公告（published + pinned）
            newsRepository.save(News.builder()
                    .title("🎉 網站正式上線，歡迎體驗！")
                    .content("本電商平台已全面上線，提供 3C 電子、居家生活等多種商品。\n\n" +
                             "您可以透過「商品列表」頁面瀏覽所有在架商品，選購後加入購物車，完成結帳即可查看訂單記錄。\n\n" +
                             "如有任何問題，歡迎聯繫客服。感謝您的支持！")
                    .pinned(true)
                    .published(true)
                    .createdAt(now.minusDays(3))
                    .updatedAt(now.minusDays(3))
                    .build());

            // 一般公告（published）
            newsRepository.save(News.builder()
                    .title("五月份限時特惠活動開跑！")
                    .content("即日起至 5 月 31 日，全站精選商品最高享 8 折優惠！\n\n" +
                             "活動商品包含：\n" +
                             "・無線藍牙耳機\n" +
                             "・人體工學辦公椅\n\n" +
                             "把握機會，趕快加入購物車吧！")
                    .pinned(false)
                    .published(true)
                    .createdAt(now.minusDays(1))
                    .updatedAt(now.minusDays(1))
                    .build());

            // 一般公告（published）
            newsRepository.save(News.builder()
                    .title("系統維護通知 — 5/20 凌晨 2:00 至 4:00")
                    .content("為提升服務品質，本平台將於 2026 年 5 月 20 日（三）凌晨 2:00 至 4:00 進行系統維護。\n\n" +
                             "維護期間暫停所有購物功能，造成不便敬請見諒。")
                    .pinned(false)
                    .published(true)
                    .createdAt(now.minusHours(6))
                    .updatedAt(now.minusHours(6))
                    .build());

            // 草稿（未發佈，前台看不到）
            newsRepository.save(News.builder()
                    .title("六月新品預告（草稿）")
                    .content("六月將引進全新品類，敬請期待！（此為草稿，尚未對外發佈）")
                    .pinned(false)
                    .published(false)
                    .createdAt(now)
                    .updatedAt(now)
                    .build());
        };
    }
}
