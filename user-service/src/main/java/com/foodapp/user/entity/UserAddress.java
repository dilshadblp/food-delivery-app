package com.foodapp.user.entity;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "user_address")
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many addresses belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)   // FK column in table
    private UserProfile user;

    @Column(name = "line1", nullable = false, length = 255)
    private String line1;

    @Column(name = "line2", length = 255)
    private String line2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String zipcode;

    @Column(length = 100)
    private String country;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Timestamp updatedAt;

    public UserAddress() {}

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public UserProfile getUser() { return user; }

    public void setUser(UserProfile user) { this.user = user; }

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
