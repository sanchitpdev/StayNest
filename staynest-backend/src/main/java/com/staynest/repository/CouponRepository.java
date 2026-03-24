package com.staynest.repository;

import com.staynest.entity.Coupon;
import com.staynest.enums.CouponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, UUID> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findByCouponStatus(CouponStatus status);

    // Check how many times a user has used a specific coupon
    @Query("""
            SELECT COUNT(bc) FROM BookingCoupon bc
            WHERE bc.coupon.couponId = :couponId
            AND bc.booking.guest.userId = :userId
            """)
    long countUsageByUser(
            @Param("couponId") UUID couponId,
            @Param("userId") UUID userId
    );
}