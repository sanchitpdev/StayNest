package com.staynest.entity;

import com.staynest.enums.PricingType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "unit_pricing", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"unit_id", "pricing_type", "start_date"})
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE unit_pricing SET deleted_at = NOW() WHERE pricing_id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "pricing_id", updatable = false, nullable = false)
    private UUID pricingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pricing_type", nullable = false)
    private PricingType pricingType;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Date range for this pricing rule (null = applies always for this type)
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", length = 200)
    private String description;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    // Helper methods
    public boolean isActiveOn(LocalDate date) {
        if (startDate == null && endDate == null) return true;
        if (startDate != null && date.isBefore(startDate)) return false;
        if (endDate != null && date.isAfter(endDate)) return false;
        return true;
    }

    public boolean isWeekend() {
        return pricingType == PricingType.WEEKEND;
    }
}