package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.UserStoreRequest;
import com.ecommerce.backend.dto.UserStoreResponse;
import com.ecommerce.backend.model.DeliveryMethod;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.UserStore;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.repository.UserStoreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserStoreService {

    private final UserStoreRepository userStoreRepository;
    private final UserRepository userRepository;

    public UserStoreService(UserStoreRepository userStoreRepository,
                            UserRepository userRepository) {
        this.userStoreRepository = userStoreRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserStoreResponse> getMyStores(String username, DeliveryMethod storeType) {
        User user = findUser(username);
        return userStoreRepository.findByUserIdAndStoreTypeOrderByCreatedAtDesc(user.getId(), storeType)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserStoreResponse addStore(String username, UserStoreRequest request) {
        User user = findUser(username);

        // 若相同超商+店號已存在，直接回傳（不重複儲存）
        if (userStoreRepository.existsByUserIdAndStoreTypeAndStoreCode(
                user.getId(), request.storeType(), request.storeCode())) {
            return userStoreRepository
                    .findByUserIdAndStoreTypeOrderByCreatedAtDesc(user.getId(), request.storeType())
                    .stream()
                    .filter(s -> s.getStoreCode().equals(request.storeCode()))
                    .findFirst()
                    .map(this::toResponse)
                    .orElseThrow();
        }

        UserStore store = UserStore.builder()
                .user(user)
                .storeType(request.storeType())
                .storeName(request.storeName())
                .storeCode(request.storeCode())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(userStoreRepository.save(store));
    }

    @Transactional
    public UserStoreResponse updateStore(String username, Long storeId, UserStoreRequest request) {
        User user = findUser(username);
        UserStore store = userStoreRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "常用門市不存在"));
        if (!store.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權限修改此門市");
        }
        store.setStoreType(request.storeType());
        store.setStoreName(request.storeName());
        store.setStoreCode(request.storeCode());
        return toResponse(userStoreRepository.save(store));
    }

    @Transactional
    public void deleteStore(String username, Long storeId) {
        User user = findUser(username);
        UserStore store = userStoreRepository.findById(storeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "常用門市不存在"));
        if (!store.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權限刪除此門市");
        }
        userStoreRepository.delete(store);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }

    private UserStoreResponse toResponse(UserStore s) {
        String label = s.getStoreType() == DeliveryMethod.FAMILY_MART ? "全家" : "7-11";
        return new UserStoreResponse(s.getId(), s.getStoreType().name(), label,
                s.getStoreName(), s.getStoreCode(), s.getCreatedAt());
    }
}
