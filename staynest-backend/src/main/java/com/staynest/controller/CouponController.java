package com.staynest.controller;

import com.staynest.dto.request.ApplyCouponRequest;
import com.staynest.dto.request.CouponCreateRequest;
import com.staynest.dto.response.ApiResponse;
import com.staynest.dto.response.ApplyCouponResponse;
import com.staynest.dto.response.CouponResponse;
import com.staynest.entity.User;
import com.staynest.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    /**
     * Create a new coupon — ADMIN only
     * POST /api/v1/coupons
     */
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(
            @Valid @RequestBody CouponCreateRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CouponResponse response = couponService.createCoupon(request, user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all active coupons — public
     * GET /api/v1/coupons
     */
    @GetMapping
    public ResponseEntity<List<CouponResponse>> getActiveCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }

    /**
     * Get all coupons including inactive — ADMIN only
     * GET /api/v1/coupons/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<CouponResponse>> getAllCoupons(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(couponService.getAllCoupons(user.getUserId()));
    }

    /**
     * Apply a coupon to a booking
     * POST /api/v1/coupons/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<ApplyCouponResponse> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ApplyCouponResponse response = couponService.applyCoupon(request, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a coupon — ADMIN only
     * PATCH /api/v1/coupons/{couponId}/deactivate
     */
    @PatchMapping("/{couponId}/deactivate")
    public ResponseEntity<CouponResponse> deactivateCoupon(
            @PathVariable UUID couponId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CouponResponse response = couponService.deactivateCoupon(couponId, user.getUserId());
        return ResponseEntity.ok(response);
    }
}