package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.DeliveryMethod;
import com.ecommerce.backend.model.UserStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserStoreRepository extends JpaRepository<UserStore, Long> {
    List<UserStore> findByUserIdAndStoreTypeOrderByCreatedAtDesc(Long userId, DeliveryMethod storeType);
    boolean existsByUserIdAndStoreTypeAndStoreCode(Long userId, DeliveryMethod storeType, String storeCode);
}
