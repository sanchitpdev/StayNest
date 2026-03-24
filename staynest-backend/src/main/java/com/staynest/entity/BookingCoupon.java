package com.staynest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_coupons", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"booking_id", "coupon_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_coupon_id", updatable = false, nullable = false)
    private UUID bookingCouponId;

    // The actual discount amount applied at time of booking
    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    // Snapshot of coupon code at time of use (in case coupon is deleted later)
    @Column(name = "coupon_code_snapshot", nullable = false, length = 50)
    private String couponCodeSnapshot;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;
}