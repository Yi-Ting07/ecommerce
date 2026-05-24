package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.UserAddressRequest;
import com.ecommerce.backend.dto.UserAddressResponse;
import com.ecommerce.backend.model.User;
import com.ecommerce.backend.model.UserAddress;
import com.ecommerce.backend.repository.UserAddressRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAddressService {

    private final UserAddressRepository userAddressRepository;
    private final UserRepository userRepository;

    public UserAddressService(UserAddressRepository userAddressRepository,
                              UserRepository userRepository) {
        this.userAddressRepository = userAddressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserAddressResponse> getMyAddresses(String username) {
        User user = findUser(username);
        return userAddressRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public UserAddressResponse addAddress(String username, UserAddressRequest request) {
        User user = findUser(username);

        // 若相同收貨人+電話+地址已存在，直接回傳
        if (userAddressRepository.existsByUserIdAndRecipientNameAndRecipientPhoneAndAddress(
                user.getId(), request.recipientName(), request.recipientPhone(), request.address())) {
            UserAddress existing = userAddressRepository
                    .findByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .filter(a -> a.getRecipientName().equals(request.recipientName())
                            && a.getRecipientPhone().equals(request.recipientPhone())
                            && a.getAddress().equals(request.address()))
                    .findFirst()
                    .orElseThrow();
            return toResponse(existing);
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .label(request.label())
                .recipientName(request.recipientName())
                .recipientPhone(request.recipientPhone())
                .address(request.address())
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(userAddressRepository.save(address));
    }

    @Transactional
    public UserAddressResponse updateAddress(String username, Long addressId, UserAddressRequest request) {
        User user = findUser(username);
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "常用地址不存在"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權限修改此地址");
        }
        address.setLabel(request.label());
        address.setRecipientName(request.recipientName());
        address.setRecipientPhone(request.recipientPhone());
        address.setAddress(request.address());
        return toResponse(userAddressRepository.save(address));
    }

    @Transactional
    public void deleteAddress(String username, Long addressId) {
        User user = findUser(username);
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "常用地址不存在"));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "無權限刪除此地址");
        }
        userAddressRepository.delete(address);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "使用者不存在"));
    }

    private UserAddressResponse toResponse(UserAddress a) {
        return new UserAddressResponse(a.getId(), a.getLabel(), a.getRecipientName(),
                a.getRecipientPhone(), a.getAddress(), a.getCreatedAt());
    }
}
