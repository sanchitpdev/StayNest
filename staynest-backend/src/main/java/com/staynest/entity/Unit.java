package com.staynest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "units",uniqueConstraints = {
        @UniqueConstraint(columnNames = {"property_id","unit_number"})
})
@EntityListeners(AuditingEntityListener.class)
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Unit {

    //========Primary Key=============
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "unit_id",updatable = false,nullable = false)
    private UUID unitId;

    //==========Basic Fields==========
    @Column(name = "unit_name",nullable = false,length = 100)
    private String unitName;

    @Column(name = "unit_number",nullable = false,length = 50)
    private String unitNumber;

    @Column(name = "bedrooms",nullable = false)
    private Integer bedrooms;

    @Column(name = "bathrooms",nullable = false,precision = 3,scale = 1)
    private BigDecimal bathrooms;

    @Column(name = "max_guest",nullable = false)
    private Integer maxGuest;

    @Column(name = "square_feet")
    private Integer squareFeet;

    //=========Pricing ==========
    @Column(name = "base_price",nullable = false,precision = 10,scale = 2)
    private BigDecimal basePrice;

    @Column(name = "cleaning_fee",precision = 10,scale = 2)
    @Builder.Default
    private BigDecimal cleaningFee = BigDecimal.ZERO;

    //==========Availability==========
    @Column(name = "is_available",nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    //==========Audit Fields ==============
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    //=========Relationship============
    //Many property belongs to one property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id",nullable = false)
    private Property property;

    //One unit has many booking
    @OneToMany(mappedBy = "unit",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    //One unit can have specific images
    @OneToMany(mappedBy = "unit",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<PropertyImage> propertyImages = new ArrayList<>();

    //=========Helper Method==================
    //Calculate total price
    public BigDecimal getTotalPrice(){
        return basePrice.add(cleaningFee);
    }

    //Calculate the price for total number of night
    public BigDecimal calculatePrice(int nights){
        if(nights <= 0 ){
            throw new IllegalArgumentException("Nights Must be Positive");
        }
        return basePrice.multiply(BigDecimal.valueOf(nights)).add(cleaningFee);
    }

    //check if unit can accommodate the given number of guests
    public boolean canAccommodate(int guests){
        return guests > 0 && guests <= maxGuest;
    }



}
