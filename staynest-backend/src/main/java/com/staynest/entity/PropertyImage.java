package com.staynest.entity;

import com.staynest.enums.BookingStatus;
import com.staynest.enums.ImageType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.web.bind.annotation.BindParam;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "property-images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    //=========Relationship========
    //Many images for one property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property-id",nullable = false)
    private Property property;

    //Many images for one unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id",nullable = true)
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
