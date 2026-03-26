package com.staynest.service;

import com.staynest.dto.request.ApplyCouponRequest;
import com.staynest.dto.request.CouponCreateRequest;
import com.staynest.dto.response.ApplyCouponResponse;
import com.staynest.dto.response.CouponResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.BookingCoupon;
import com.staynest.entity.Coupon;
import com.staynest.entity.User;
import com.staynest.enums.BookingStatus;
import com.staynest.enums.CouponStatus;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.BookingCouponRepository;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.CouponRepository;
import com.staynest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CouponService {

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BookingCouponRepository bookingCouponRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new coupon — ADMIN only
     */
    @Transactional
    public CouponResponse createCoupon(CouponCreateRequest request, UUID userId) {
        logger.info("Creating coupon with code: {} by user {}", request.getCode(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (!user.isAdmin()) {
            throw new UnauthorizedException("Only administrators can create coupons");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minBookingAmount(request.getMinBookingAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .maxUsageLimit(request.getMaxUsageLimit())
                .perUserLimit(request.getPerUserLimit())
                .build();

        Coupon saved = couponRepository.save(coupon);
        logger.info("Coupon {} created successfully", saved.getCode());

        return buildCouponResponse(saved);
    }

    /**
     * Apply a coupon to a booking
     */
    @Transactional
    public ApplyCouponResponse applyCoupon(ApplyCouponRequest request, UUID userId) {
        logger.info("Applying coupon {} to booking {}", request.getCouponCode(), request.getBookingId());

        // Find booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));

        // Only the guest who made the booking can apply a coupon
        if (!booking.getGuest().getUserId().equals(userId)) {
            throw new BadRequestException("You can only apply coupons to your own bookings");
        }

        // Can only apply coupon to PENDING bookings
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new BadRequestException("Coupons can only be applied to PENDING bookings");
        }

        // Check if coupon already applied to this booking
        List<BookingCoupon> existing = bookingCouponRepository.findByBooking_BookingId(request.getBookingId());
        if (!existing.isEmpty()) {
            throw new BadRequestException("A coupon has already been applied to this booking");
        }

        // Find and validate coupon
        Coupon coupon = couponRepository.findByCode(request.getCouponCode().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + request.getCouponCode()));

        if (!coupon.isValid()) {
            throw new BadRequestException("Coupon is expired or inactive: " + request.getCouponCode());
        }

        if (!coupon.isApplicableFor(booking.getTotalPrice())) {
            throw new BadRequestException(
                    String.format("Minimum booking amount of ₹%s required for this coupon",
                            coupon.getMinBookingAmount())
            );
        }

        // Check per-user usage limit
        long userUsageCount = couponRepository.countUsageByUser(coupon.getCouponId(), userId);
        if (userUsageCount >= coupon.getPerUserLimit()) {
            throw new BadRequestException("You have already used this coupon the maximum number of times");
        }

        // Calculate discount
        BigDecimal discountAmount = coupon.calculateDiscount(booking.getTotalPrice());
        BigDecimal finalPrice = booking.getTotalPrice().subtract(discountAmount);

        // Apply to booking
        booking.setDiscountAmount(discountAmount);
        booking.setFinalPrice(finalPrice);
        bookingRepository.save(booking);

        // Record the coupon usage
        BookingCoupon bookingCoupon = BookingCoupon.builder()
                .booking(booking)
                .coupon(coupon)
                .discountAmount(discountAmount)
                .couponCodeSnapshot(coupon.getCode())
                .build();
        bookingCouponRepository.save(bookingCoupon);

        // Increment coupon usage count
        coupon.incrementUsage();
        couponRepository.save(coupon);

        logger.info("Coupon {} applied to booking {} - discount: {}",
                coupon.getCode(), booking.getBookingId(), discountAmount);

        return ApplyCouponResponse.builder()
                .bookingId(booking.getBookingId().toString())
                .couponCode(coupon.getCode())
                .originalPrice(booking.getTotalPrice())
                .discountAmount(discountAmount)
                .finalPrice(finalPrice)
                .message(String.format("Coupon applied successfully! You saved ₹%s", discountAmount))
                .build();
    }

    /**
     * Get all active coupons — public
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getActiveCoupons() {
        return couponRepository.findByCouponStatus(CouponStatus.ACTIVE)
                .stream()
                .map(this::buildCouponResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all coupons — ADMIN only
     */
    @Transactional(readOnly = true)
    public List<CouponResponse> getAllCoupons(UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminId));

        if (!admin.isAdmin()) {
            throw new UnauthorizedException("Only administrators can view all coupons");
        }

        return couponRepository.findAll()
                .stream()
                .map(this::buildCouponResponse)
                .collect(Collectors.toList());
    }


    /**
     * Deactivate a coupon — ADMIN only
     */
    @Transactional
    public CouponResponse deactivateCoupon(UUID couponId, UUID adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminId));

        if (!admin.isAdmin()) {
            throw new UnauthorizedException("Only administrators can deactivate coupons");
        }

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));

        coupon.setCouponStatus(CouponStatus.INACTIVE);
        couponRepository.save(coupon);
        logger.info("Coupon {} deactivated by admin {}", coupon.getCode(), adminId);

        return buildCouponResponse(coupon);
    }

    // Helper method
    private CouponResponse buildCouponResponse(Coupon coupon) {
        return CouponResponse.builder()
                .couponId(coupon.getCouponId().toString())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minBookingAmount(coupon.getMinBookingAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .validFrom(coupon.getValidFrom())
                .validUntil(coupon.getValidUntil())
                .maxUsageLimit(coupon.getMaxUsageLimit())
                .perUserLimit(coupon.getPerUserLimit())
                .usageCount(coupon.getUsageCount())
                .couponStatus(coupon.getCouponStatus())
                .createdAt(coupon.getCreatedAt())
                .build();
    }
}