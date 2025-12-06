package com.foodapp.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


public class UpdateProfileRequest {

    @Size(max = 255, message = "Full name too long")
    private String fullName;

    @Size(max = 50, message = "Phone too long")
    private String phone;

    public UpdateProfileRequest() {}

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }

    public void setPhone(String phone) { this.phone = phone; }
}
