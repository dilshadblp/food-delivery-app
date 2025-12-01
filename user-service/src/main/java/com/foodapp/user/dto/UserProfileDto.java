package com.foodapp.user.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


public class UserProfileDto {

    private Long id;
    private String email;
    private String fullName;
    private String phone;

    // All addresses for this user
    private List<UserAddressDto> addresses = new ArrayList<>();

    public UserProfileDto() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }

    public List<UserAddressDto> getAddresses() { return addresses; }

    public void setAddresses(List<UserAddressDto> addresses) { this.addresses = addresses; }
}
