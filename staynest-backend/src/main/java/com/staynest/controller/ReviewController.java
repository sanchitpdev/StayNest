package com.staynest.controller;

import com.staynest.dto.request.HostResponseRequest;
import com.staynest.dto.request.ReviewCreateRequest;
import com.staynest.dto.response.BookingResponse;
import com.staynest.dto.response.PagedResponse;
import com.staynest.dto.response.ReviewCreateResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.User;
import com.staynest.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for review management
 * Handles creating and retrieving reviews
 */
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * Create a review for booking
     * Only guest can review after checkOut

     * POST /api/v1/reviews
     */
    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        ReviewCreateResponse response = reviewService.createReview(request, user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get review by I'd
     * Public endpoint - anyone can view reviews

     * GET /api/v1/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewCreateResponse> getReviewById(
            @PathVariable UUID reviewId
            ){
        ReviewCreateResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }


    /**
     * Get reviews for a property with pagination
     * GET /api/v1/reviews/property/{propertyId}?page=0&size=10
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<PagedResponse<ReviewCreateResponse>> getReviewsByProperty(
            @PathVariable UUID propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsByProperty(propertyId, page, size));
    }

    /**
     * Get all reviews written by user.

     * GET /api/v1/reviews/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewCreateResponse>> getReviewsByUser(
            @PathVariable UUID userId
    ){
        List<ReviewCreateResponse> reviews = reviewService.getReviewsByUser(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get my reviews (as guest)

     * GET /api/v1/reviews/my-reviews
     */
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewCreateResponse>> getMyReviews(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<ReviewCreateResponse> reviews = reviewService.getReviewsByUser(user.getUserId());
        return ResponseEntity.ok(reviews);
    }

    /**
     * Get Bookings that can be reviewed

     * GET /api/v1/reviews/reviewable-bookings
     */
    public ResponseEntity<List<BookingResponse>> getReviewableBookings(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<BookingResponse> bookings = reviewService.getReviewableBookingResponses(user.getUserId());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Host responds to a review
     * POST /api/v1/reviews/{reviewId}/host-response
     */
    @PostMapping("/{reviewId}/host-response")
    public ResponseEntity<ReviewCreateResponse> addHostResponse(
            @PathVariable UUID reviewId,
            @Valid @RequestBody HostResponseRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ReviewCreateResponse response = reviewService.addHostResponse(
                reviewId, request.getResponse(), user.getUserId());
        return ResponseEntity.ok(response);
    }
}
