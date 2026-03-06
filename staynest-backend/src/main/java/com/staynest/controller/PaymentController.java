package com.staynest.controller;

import com.staynest.dto.request.PaymentCreateRequest;
import com.staynest.dto.response.PaymentResponse;
import com.staynest.entity.User;
import com.staynest.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for payment management
 */
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Create a payment (record payment for booking)

     * POST /api/v1/payments
     */
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentCreateRequest request,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        PaymentResponse response = paymentService.createPayment(request,user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get payment by ID
     * Only guest or host can view.

     * GET /api/v1/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable UUID paymentId,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        PaymentResponse response = paymentService.getPaymentById(paymentId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments for bookings

     * GET /api/v1/payments/bookings/{bookingId}
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentByBooking(
            @PathVariable UUID bookingId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        List<PaymentResponse> payments = paymentService.getPaymentByBooking(bookingId, user.getUserId());
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all payments (as guest)

     * GET /api/v1/payments/my-payments
     */
    @GetMapping("my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<PaymentResponse> payments = paymentService.getMyPayments(user.getUserId());
        return ResponseEntity.ok(payments);
    }

}
