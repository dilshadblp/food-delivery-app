package com.foodapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class CreateOrUpdateAddressRequest {

    @NotBlank(message = "Address line1 is required")
    @Size(max = 255, message = "line1 too long")
    private String line1;

    @Size(max = 255, message = "line2 too long")
    private String line2;

    @Size(max = 100, message = "City too long")
    private String city;

    @Size(max = 100, message = "State too long")
    private String state;

    @Size(max = 20, message = "Zipcode too long")
    private String zipcode;

    @Size(max = 100, message = "Country too long")
    private String country;

    // Whether this should become the default address
    private boolean defaultAddress;

    public CreateOrUpdateAddressRequest() {}

    public String getLine1() { return line1; }

    public void setLine1(String line1) { this.line1 = line1; }

    public String getLine2() { return line2; }

    public void setLine2(String line2) { this.line2 = line2; }

    public String getCity() { return city; }

    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }

    public void setState(String state) { this.state = state; }

    public String getZipcode() { return zipcode; }

    public void setZipcode(String zipcode) { this.zipcode = zipcode; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

    public boolean isDefaultAddress() { return defaultAddress; }

    public void setDefaultAddress(boolean defaultAddress) { this.defaultAddress = defaultAddress; }
}
