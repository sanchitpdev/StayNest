package com.staynest.entity;

import com.staynest.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class User {
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
    @Column(name = "role",nullable = false)
    private UserRole role;

    @Column(name = "is_verified",nullable = false)
    @Builder.Default
    private Boolean isVerified = false;

    //========AUDIT FIELDS===========
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //===========RELATIONSHIPS==========
    //one user can host many properties
    @OneToMany(mappedBy = "host",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Property> hostedProperties = new ArrayList<>();

    //one user(as guest) can make many bookings
    @OneToMany(mappedBy = "guest",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    //one user can have many wishlist
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<>();

    //one user can write many Reviews
    @OneToMany(mappedBy = "reviewer",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
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
    public boolean isGuest(){
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
}
