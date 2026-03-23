package com.staynest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staynest.enums.CancellationPolicy;
import com.staynest.enums.PropertyStatus;
import com.staynest.enums.PropertyType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import io.hypersistence.utils.hibernate.type.json.JsonType;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE properties SET deleted_at = NOW() WHERE property_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Property {
    //=======PRIMARY KEY=============
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "property_id",nullable = false,updatable = false)
    private UUID propertyId;

    //==========BASIC FIELDS==========
    @Column(name = "property_name",nullable = false,length = 200)
    private String propertyName;

    @Column(name = "description" , columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_type", nullable = false)
    private PropertyType propertyType;

    //=========Address Fields ==========
    @Column(name = "address", nullable = false, length = 500)
    private String streetAddress;          // renamed from 'address' → 'streetAddress'

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "country", nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    // V2 - New normalized address relationship
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private Address address;

    //Json fields(amenities)
    @Type(JsonType.class)
    @Column(name = "amenities",columnDefinition = "jsonb")
    private Map<String, Object> amenities;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_status", nullable = false)
    @Builder.Default
    private PropertyStatus propertyStatus = PropertyStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false)
    @Builder.Default
    private CancellationPolicy cancellationPolicy = CancellationPolicy.MODERATE;

    @Column(name = "min_stay_nights")
    @Builder.Default
    private Integer minStayNights = 1;

    @Column(name = "max_stay_nights")
    private Integer maxStayNights;

    //========Audit fields======
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //======Relationship=========
    //Many properties belongs to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "hostedProperties", "bookings", "wishlists", "reviews", "passwordHash"})
    private User host;

    //one property has many units
    @OneToMany(mappedBy = "property",cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
    @Builder.Default
    private List<Unit> units = new ArrayList<>();

    //one property has many images
    @OneToMany(mappedBy = "property",cascade = CascadeType.ALL,fetch = FetchType.LAZY,orphanRemoval = true)
    @Builder.Default
    private List<PropertyImage> images = new ArrayList<>();

    //One property receives many reviews
    @OneToMany(mappedBy = "property",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    //One property have many wishlist
    @OneToMany(mappedBy = "property",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Wishlist> wishlists = new ArrayList<>();

    //=======Helper Methods==========
    //Get Full address
    public String getFullAddress() {
        return String.format("%s, %s, %s, %s, %s",
                streetAddress, city, state, country, postalCode);
    }
    //get total number of property in this unit
    public int getTotalUnit(){
        return units != null ? units.size() : 0;
    }

    //add unit to this property
    public void addUnit(Unit unit){
        units.add(unit);
        unit.setProperty(this);
    }

    //remove unit to this property
    public void removeUnit(Unit unit){
        units.remove(unit);
        unit.setProperty(null);
    }

    public boolean isBookable() {
        return propertyStatus == PropertyStatus.ACTIVE;
    }
}
