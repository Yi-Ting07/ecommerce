package com.ecommerce.backend.repository;

import com.ecommerce.backend.model.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByCreatedAtDesc(Long userId);
    boolean existsByUserIdAndRecipientNameAndRecipientPhoneAndAddress(
            Long userId, String recipientName, String recipientPhone, String address);
}
