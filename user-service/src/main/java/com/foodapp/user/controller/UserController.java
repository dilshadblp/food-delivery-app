package com.foodapp.user.controller;

import com.foodapp.common.ApiException;
import com.foodapp.user.dto.CreateOrUpdateAddressRequest;
import com.foodapp.user.dto.UpdateProfileRequest;
import com.foodapp.user.dto.UserProfileDto;
import com.foodapp.user.dto.UserAddressDto;
import com.foodapp.user.service.UserProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserProfileService profileService;

    public UserController(UserProfileService profileService) {
        this.profileService = profileService;
    }

    // Extract userId from header (later from JWT)
    private Long getUserId(HttpServletRequest request) {
        String id = request.getHeader("X-User-Id");
        if (id == null) {
            throw new ApiException(401, "Missing X-User-Id header");
        }
        return Long.parseLong(id);
    }

    private String getEmail(HttpServletRequest request) {
        String email = request.getHeader("X-User-Email");
        if (email == null) {
            throw new ApiException(401, "Missing X-User-Email header");
        }
        return email;
    }

    // =============================
    //        GET PROFILE
    // =============================
    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(HttpServletRequest request) {
        Long userId = getUserId(request);
        String email = getEmail(request);

        UserProfileDto dto = profileService.getOrCreateProfile(userId, email);
        return ResponseEntity.ok(dto);
    }

    // =============================
    //      UPDATE PROFILE
    // =============================
    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest updateRequest
    ) {
        Long userId = getUserId(request);
        UserProfileDto dto = profileService.updateProfile(userId, updateRequest);
        return ResponseEntity.ok(dto);
    }

    // =============================
    //        ADD ADDRESS
    // =============================
    @PostMapping("/addresses")
    public ResponseEntity<UserAddressDto> addAddress(
            HttpServletRequest request,
            @RequestBody CreateOrUpdateAddressRequest addressRequest
    ) {
        Long userId = getUserId(request);
        UserAddressDto dto = profileService.addAddress(userId, addressRequest);
        return ResponseEntity.ok(dto);
    }

    // =============================
    //       DELETE ADDRESS
    // =============================
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        Long userId = getUserId(request);
        profileService.deleteAddress(userId, id);
        return ResponseEntity.noContent().build();
    }
}
