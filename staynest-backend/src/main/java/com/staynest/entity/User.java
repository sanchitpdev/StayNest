package com.staynest.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.staynest.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails{
    //=============primary key==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id",updatable = false,nullable = false)
    private UUID userId;
    //=============Basic Fields==========
    @Column(name = "email",unique = true,nullable = false,length = 255)
    private String email;

    @Column(name = "password_hash",nullable = false,length = 255)
    private String passwordHash;

    @Column(name = "first_name",nullable = false,length = 100)
    private String firstName;

    @Column(name = "last_name",nullable = false,length = 100)
    private String lastName;

    @Column(name = "phone_number",length = 20)
    private String phoneNumber;

    @Column(name = "profile_picture_url",length = 500)
    private String profilePictureUrl;

    //===========ENUM FIELDS===========
    @Enumerated(EnumType.STRING)
    @Column(name = "role",nullable = false) UserRole role;

    @Column(name = "is_verified",nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    //========AUDIT FIELDS===========
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //===========RELATIONSHIPS==========
    //one user can host many properties
    @OneToMany(mappedBy = "host", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Property> hostedProperties = new ArrayList<>();

    //one user(as guest) can make many bookings
    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

    //one user can have many wishlist
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Wishlist> wishlists = new ArrayList<>();

    //one user can write many Reviews
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    //=============HELPER METHOD============
    //get full name of user
    public String getFullName(){
        return firstName +" " +lastName;
    }

    //check user has a host role
    public boolean isHost(){
        return role == UserRole.HOST;
    }

    //check user has a guest role
    public boolean isGuestOrHost    (){
        return role == UserRole.GUEST || role == UserRole.HOST;
    }

    //lifecycle callbacks
    @PrePersist
    protected void onCreate(){
        if (createdAt == null){
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null){
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = LocalDateTime.now();
    }


    //==================Spring Security UserDetails Implementation================
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


}
