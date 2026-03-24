package com.staynest.entity;

import com.staynest.enums.CouponStatus;
import com.staynest.enums.DiscountType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "coupons")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE coupons SET deleted_at = NOW() WHERE coupon_id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "coupon_id", updatable = false, nullable = false)
    private UUID couponId;

    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    // Minimum booking amount required to apply this coupon
    @Column(name = "min_booking_amount", precision = 10, scale = 2)
    private BigDecimal minBookingAmount;

    // Maximum discount cap (useful for percentage coupons)
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    // How many times this coupon can be used in total (null = unlimited)
    @Column(name = "max_usage_limit")
    private Integer maxUsageLimit;

    // How many times one user can use this coupon
    @Column(name = "per_user_limit")
    @Builder.Default
    private Integer perUserLimit = 1;

    @Column(name = "usage_count", nullable = false)
    @Builder.Default
    private Integer usageCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_status", nullable = false)
    @Builder.Default
    private CouponStatus couponStatus = CouponStatus.ACTIVE;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingCoupon> bookingCoupons = new ArrayList<>();

    // Helper methods
    public boolean isValid() {
        LocalDate today = LocalDate.now();
        return couponStatus == CouponStatus.ACTIVE
                && !today.isBefore(validFrom)
                && !today.isAfter(validUntil)
                && (maxUsageLimit == null || usageCount < maxUsageLimit);
    }

    public boolean isApplicableFor(BigDecimal bookingAmount) {
        if (!isValid()) return false;
        if (minBookingAmount != null && bookingAmount.compareTo(minBookingAmount) < 0) {
            return false;
        }
        return true;
    }

    public BigDecimal calculateDiscount(BigDecimal bookingAmount) {
        BigDecimal discount;

        if (discountType == DiscountType.PERCENTAGE) {
            discount = bookingAmount.multiply(discountValue)
                    .divide(BigDecimal.valueOf(100));
            // Apply max cap if set
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            // FLAT discount — can't exceed booking amount
            discount = discountValue.min(bookingAmount);
        }

        return discount;
    }

    public void incrementUsage() {
        this.usageCount++;
        // Auto-expire if usage limit reached
        if (maxUsageLimit != null && usageCount >= maxUsageLimit) {
            this.couponStatus = CouponStatus.EXPIRED;
        }
    }
}