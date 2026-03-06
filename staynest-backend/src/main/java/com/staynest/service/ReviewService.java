package com.staynest.service;

import com.staynest.dto.request.ReviewCreateRequest;
import com.staynest.dto.response.ReviewCreateResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.Review;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for review management operations
 */
@Service
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request, UUID userId){
        logger.info("Creating review for booking {} by user {}", request.getBookingId(), userId);

        //Step 1: Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + request.getBookingId()));

        //Step 2: Check authorization(only guest can review)
        if (!booking.getGuest().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only review your own bookings");
        }

        //Step 3: Check if bookings can be reviewed
        if (!booking.canBeReviewed()){
            throw new BadRequestException("Can only review completed bookings after checkout. Current status: "+ booking.getBookingStatus());
        }

        //Step 4: Check if checkout date has passed
        if (!LocalDate.now().isAfter(booking.getCheckOutDate())){
            throw new BadRequestException("You can only review  after checkout date");
        }

        //Step 5: Check if review already exists
        if (reviewRepository.findByBooking_BookingId(booking.getBookingId()).isPresent()){
            throw new BadRequestException("You can already reviewed this booking");
        }

        //Step 6: Create review entity
        Review review = Review.builder()
                .booking(booking)
                .property(booking.getUnit().getProperty())
                .reviewer(booking.getGuest())
                .rating(request.getRating())
                .comment(request.getComment())
                .cleanlinessRating(request.getCleanlinessRating())
                .accuracyRating(request.getAccuracyRating())
                .communicationRating(request.getCommunicationRating())
                .locationRating(request.getLocationRating())
                .valueRating(request.getValueRating())
                .build();

        //Step 7: Save review
        Review savedReview = reviewRepository.save(review);
        logger.info("Review created successfully with ID: {}" ,savedReview.getReviewer());

        //Step 8: Build and return response
        return buildReviewResponse(savedReview);
    }

    /**
     * Get review bu id
     * @param reviewId - Review I'd
     * @return ReviewCreateResponse
     */
    @Transactional(readOnly = true)
    public ReviewCreateResponse getReviewById(UUID reviewId){
        logger.info("Fetching review {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: "+ reviewId));

        return buildReviewResponse(review);
    }

    /**
     * Get all reviews for property
     * @param propertyId  - Property ID
     * @return List of ReviewCreateResponse
     */
    @Transactional(readOnly = true)
    public List<ReviewCreateResponse> getReviewByProperty(UUID propertyId){
        logger.info("Fetching reviews for property {} ", propertyId);

        List<Review> reviews = reviewRepository.findByProperty_PropertyId(propertyId);
        return reviews.stream()
                .map(this::buildReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all reviews written by user
     * @param userId - User Id
     * @return List of ReviewCreateResponse
     */
    @Transactional(readOnly = true)
    public List<ReviewCreateResponse> getReviewsByUser(UUID userId){
        logger.info("Fetching reviews by user {}", userId);

        List<Review> reviews = reviewRepository.findByReviewer_UserId(userId);
        return reviews.stream()
                .map(this::buildReviewResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get average rating for property
     * @param propertyId - property I'd
     * @return Average rating (null if no reviews
     */
    @Transactional(readOnly = true)
    public Double getAverageRating(UUID propertyId){
        logger.info("Calculating average rating for property {} ", propertyId);
        return reviewRepository.calculateAverageRating(propertyId);
    }

    /**
     * Get bookings that can be reviewed by user
     * @param userId - userId
     * @return List of bookings that can be reviewed
     */
    @Transactional(readOnly = true)
    public List<Booking> getReviewableBookings(UUID userId){
        logger.info("Fetching reviewable bookings for user {}", userId);
        return bookingRepository.findReviewableBookings(userId, LocalDate.now());
    }

    /**
     * Helper method to build ReviewCreateRepose from review entity
     */
    private ReviewCreateResponse buildReviewResponse(Review review) {
        return ReviewCreateResponse.builder()
                .reviewId(review.getReviewId().toString())
                .bookingId(review.getBooking().getBookingId().toString())
                .propertyId(review.getProperty().getPropertyId().toString())
                .propertyName(review.getProperty().getPropertyName())
                .reviewerId(review.getReviewer().getUserId().toString())
                .reviewerName(review.getReviewer().getFullName())
                .rating(review.getRating())
                .comment(review.getComment())
                .cleanlinessRating(review.getCleanlinessRating())
                .accuracyRating(review.getAccuracyRating())
                .communicationRating(review.getCommunicationRating())
                .locationRating(review.getLocationRating())
                .valueRating(review.getValueRating())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
