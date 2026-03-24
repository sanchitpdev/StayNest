package com.staynest.repository;

import com.staynest.entity.BookingCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookingCouponRepository extends JpaRepository<BookingCoupon, UUID> {

    List<BookingCoupon> findByBooking_BookingId(UUID bookingId);
}