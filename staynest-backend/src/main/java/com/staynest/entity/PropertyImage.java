package com.staynest.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staynest.enums.BookingStatus;
import com.staynest.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "property_images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE property_images SET deleted_at = NOW() WHERE image_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class PropertyImage {
    //=========Primary Key==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_id",unique = true,nullable = false)
    private UUID imageId;

    //============Image Fields========
    @Column(name = "image_url",nullable = false,length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type")
    private ImageType imageType;

    @Column(name = "is_primary",nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "display_order")
    private Integer displayOrder;

    //==========Audit Fields===========
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //=========Relationship========
    //Many images for one property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "host", "images", "units", "reviews", "wishlists"})
    private Property property;

    //Many images for one unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "propertyImages", "property", "bookings"})
    private Unit unit;

    //==========Helper method========
    //Check if this is a property-level image
    public boolean isPropertyImage(){
        return unit == null;
    }

    //check if this is unit-specific image
    public boolean isUnitImage(){
        return unit != null;
    }

}
