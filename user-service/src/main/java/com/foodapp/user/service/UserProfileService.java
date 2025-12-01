package com.foodapp.user.service;

import com.foodapp.common.ApiException;
import com.foodapp.user.dto.CreateOrUpdateAddressRequest;
import com.foodapp.user.dto.UserAddressDto;
import com.foodapp.user.dto.UserProfileDto;
import com.foodapp.user.dto.UpdateProfileRequest;
import com.foodapp.user.entity.UserAddress;
import com.foodapp.user.entity.UserProfile;
import com.foodapp.user.repo.UserAddressRepository;
import com.foodapp.user.repo.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserAddressRepository userAddressRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              UserAddressRepository userAddressRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userAddressRepository = userAddressRepository;
    }

    @Transactional
    public UserProfileDto getOrCreateProfile(Long userId, String email) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setId(userId);
                    p.setEmail(email);
                    return userProfileRepository.save(p);
                });

        return toDto(profile);
    }

    @Transactional
    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "User profile not found"));

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            profile.setPhone(request.getPhone());
        }

        UserProfile saved = userProfileRepository.save(profile);
        return toDto(saved);
    }

    @Transactional
    public UserAddressDto addAddress(Long userId, CreateOrUpdateAddressRequest request) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new ApiException(404, "User profile not found"));

        if (request.isDefaultAddress()) {
            List<UserAddress> existing = userAddressRepository.findByUserId(userId);
            for (UserAddress addr : existing) {
                addr.setDefaultAddress(false);
            }
            userAddressRepository.saveAll(existing);
        }

        UserAddress address = new UserAddress();
        address.setUser(profile);
        address.setLine1(request.getLine1());
        address.setLine2(request.getLine2());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZipcode(request.getZipcode());
        address.setCountry(request.getCountry());
        address.setDefaultAddress(request.isDefaultAddress());

        UserAddress saved = userAddressRepository.save(address);
        return toDto(saved);
    }

    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        UserAddress address = userAddressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ApiException(404, "Address not found"));
        userAddressRepository.delete(address);
    }

    private UserProfileDto toDto(UserProfile profile) {
        UserProfileDto dto = new UserProfileDto();
        dto.setId(profile.getId());
        dto.setEmail(profile.getEmail());
        dto.setFullName(profile.getFullName());
        dto.setPhone(profile.getPhone());

        if (profile.getAddresses() != null) {
            List<UserAddressDto> addrDtos = profile.getAddresses().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            dto.setAddresses(addrDtos);
        }

        return dto;
    }

    private UserAddressDto toDto(UserAddress address) {
        UserAddressDto dto = new UserAddressDto();
        dto.setId(address.getId());
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipcode(address.getZipcode());
        dto.setCountry(address.getCountry());
        dto.setDefaultAddress(address.isDefaultAddress());
        return dto;
    }
}
