package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.ProductRequest;
import com.ecommerce.backend.dto.ProductResponse;
import com.ecommerce.backend.model.Category;
import com.ecommerce.backend.model.Product;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getPublicProducts(String keyword, Long categoryId, Pageable pageable) {
        Specification<Product> spec = buildSpec(keyword, categoryId, true);
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getPublicProductById(Long id) {
        Product product = findEntityById(id);
        if (!product.isActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到商品");
        }
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAdminProducts(String keyword, Long categoryId, Boolean active, Pageable pageable) {
        Specification<Product> spec = buildSpec(keyword, categoryId, active);
        return productRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getAdminProductById(Long id) {
        return toResponse(findEntityById(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = categoryService.findEntityById(request.categoryId());
        LocalDateTime now = LocalDateTime.now();

        Product product = Product.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .imageUrl(request.imageUrl())
                .active(request.active() == null || request.active())
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntityById(id);
        Category category = categoryService.findEntityById(request.categoryId());

        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        product.setActive(request.active() == null || request.active());
        product.setCategory(category);
        product.setUpdatedAt(LocalDateTime.now());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntityById(id);
        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Product findEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "找不到商品"));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getImageUrl(),
                product.isActive(),
                product.getCategory().getId(),
                product.getCategory().getName()
        );
    }

    private Specification<Product> buildSpec(String keyword, Long categoryId, Boolean active) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), likeKeyword),
                    cb.like(cb.lower(root.get("description")), likeKeyword)
            ));
        }

        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }

        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }

        return spec;
    }
}
