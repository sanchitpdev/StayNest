package com.staynest.service;

import com.staynest.dto.request.BookingCreateRequest;
import com.staynest.dto.response.BookingCreateResponse;
import com.staynest.dto.response.BookingResponse;
import com.staynest.dto.response.PaymentResponse;
import com.staynest.dto.response.ReviewCreateResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.Unit;
import com.staynest.entity.User;
import com.staynest.enums.BookingStatus;
import com.staynest.enums.PaymentStatus;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.UnitRepository;
import com.staynest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for booking management operations
 */
@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AvailabilityCalendarService availabilityCalendarService;

    /**
     * Create a new Booking
     * @param request - Booking Details
     * @param userId - ID of guest making the booking
     * @return BookingCreateResponse
     */
    @Transactional
    public BookingCreateResponse createBooking(BookingCreateRequest request, UUID userId){
        logger.info("Creating booking for unit {} by user {}", request.getUnitId(), userId);

        //Step 1: Validation date range
        if (!request.isValidDateRange()){
            throw new BadRequestException("Check-out  date must be after check-in date");
        }

        //Step 2: Find the unit
        Unit unit = unitRepository.findById(request.getUnitId())
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with id: " + request.getUnitId()));

        //Step 3: Check unit is available
        if (!unit.getIsAvailable()){
            throw new BadRequestException("Unit is not available for booking");
        }

        //Step 4: Find the guest
        User guest = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userId" + userId));

        //Step 5: Check guest capacity
        if (!unit.canAccommodate(request.getNumGuests())){
            throw new BadRequestException(
                    String.format("Unit can accommodate maximum %d guests. You requested %d guests.",
                            unit.getMaxGuests(), request.getNumGuests())
            );
        }

        //Step 6: Check for date conflicts
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getUnitId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        );

        if (!overlappingBookings.isEmpty()){
            logger.warn("Booking conflicts detected for unit {} on dates {} to {}",
                    request.getUnitId(), request.getCheckInDate(),request.getCheckOutDate());
            throw new BadRequestException(
                    "Unit is already booked for the selected dates. Please choose different dates."
            );
        }

        //Step 7: Calculate number of nights
        long numberOfNights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());

        //Step 8: Calculate total price
        BigDecimal totalPrice = unit.calculatePrice((int) numberOfNights);

        //Step 9: Create booking entity
        Booking booking = Booking.builder()
                .unit(unit)
                .guest(guest)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .numGuests(request.getNumGuests())
                .specialRequest(request.getSpecialRequest())
                .totalPrice(totalPrice)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        //step 10: Save booking
        Booking saveBooking = bookingRepository.save(booking);
        logger.info("Booking created  successfully with ID: {}", saveBooking.getBookingId());

        //Step 11: Build and return response
        return BookingCreateResponse.builder()
                .bookingId(saveBooking.getBookingId().toString())
                .unitId(unit.getUnitId().toString())
                .unitName(unit.getUnitName())
                .propertyId(unit.getProperty().getPropertyId().toString())
                .propertyName(unit.getProperty().getPropertyName())
                .guestId(guest.getUserId().toString())
                .guestName(guest.getFullName())
                .checkInDate(saveBooking.getCheckInDate())
                .checkOutDate(saveBooking.getCheckOutDate())
                .numberOfNights((int)numberOfNights)
                .numGuest(saveBooking.getNumGuests())
                .specialRequests(saveBooking.getSpecialRequest())
                .totalPrice(saveBooking.getTotalPrice())
                .bookingStatus(saveBooking.getBookingStatus())
                .createdAt(saveBooking.getCreatedAt())
                .build();
    }

    /**
     * Get Booking by ID with full details
     * @param bookingId - Booking ID
     * @param userId - ID of user requesting (for authorization)
     * @return BookingResponse
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId, UUID userId){
        logger.info("Fetching booking {} for user {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID : " + bookingId));

        //Check authorization : Only guest or property host can view booking
        if (!booking.getGuest().getUserId().equals(userId) &&
            !booking.getUnit().getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only view your own bookings");
        }

        return buildBookingResponse(booking);
    }

    /**
     * Get all booking for current user (guest)
     * @param userId - Guest user ID
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(UUID userId){

        logger.info("Fetching bookings for user {}", userId);

        List<Booking> bookings = bookingRepository.findByGuest_UserId(userId);
        return bookings.stream()
                .map(this::buildBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all bookings for property
     * @param propertyId - PropertyId
     * @param userId - Host User ID (for auth)
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getPropertyBooking(UUID propertyId, UUID userId){
        logger.info("Fetching bookings for property {} by user {}",propertyId,userId);

        List<Booking> bookings = bookingRepository.findByPropertyId(propertyId);

        //Verify at least one booking exists to check authorization
        if (!bookings.isEmpty()){
            //Check if user is the property host
            if (!bookings.get(0).getUnit().getProperty().getHost().getUserId().equals(userId)){
                throw new UnauthorizedException("You can only view bookings for your own properties");
            }
        }

        return bookings.stream()
                .map(this::buildBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get upcoming bookings for guest
     * @param userId - Guest user ID
     * @return List of BookingResponse
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUpcomingBookings(UUID userId){
        logger.info("Fetching upcoming bookings for user {}", userId);

        List<Booking> bookings = bookingRepository.findUpcomingBookingsByGuest(userId,LocalDate.now());
        return bookings.stream()
                .map(this::buildBookingResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelBooking(UUID bookingId,UUID userId){
        logger.info("Cancelling booking {} by user {}", bookingId,userId);

        //Find booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        //check authorization: only guest can cancel
        if (!booking.getGuest().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only cancel your own bookings ");
        }

        //Check if booking can be canceled
        if (!booking.canBeCancelled()){
            throw new BadRequestException(
                    "Cannot cancel booking with status: " + booking.getBookingStatus()
            );
        }

        //cancel booking
        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Release calendar dates
        availabilityCalendarService.releaseDates(
                booking.getUnit().getUnitId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        logger.info("Booking {} cancelled successfully" ,bookingId);
    }

    /**
     * Confirm a booking (Host approves)
     * @param bookingId - Booking Id
     * @param userId - Host user Id
     * @return BookingResponse
     */
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, UUID userId){
        logger.info("Confirming booking {} by host {}" , bookingId, userId);

        //Find Booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: "+ bookingId));

        //Check authorization: Only property host can confirm
        if (!booking.getUnit().getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only confirm bookings for your own properties");
        }

        //check if booking is in pending status
        if (booking.getBookingStatus() != BookingStatus.PENDING){
            throw new BadRequestException(
                    "Only PENDING bookings can be confirmed. Current status: "+  booking.getBookingStatus()
            );
        }

        //Confirm booking
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Booking confirmedBooking = bookingRepository.save(booking);

        //Block Calender Dated
        availabilityCalendarService.blockDates(
                booking.getUnit().getUnitId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getBookingId()
        );
        logger.info("Booking {} confirmed successfully", bookingId);

        return buildBookingResponse(confirmedBooking);
    }

    /**
     * check unit availability for given dates
     * @param unitId - Unit I'd
     * @param checkIn - check-In date
     * @param checkOut - check out date
     * @return true if available, false if occupied
     */
    @Transactional(readOnly = true)
    public boolean checkAvailability(UUID unitId, LocalDate checkIn, LocalDate checkOut){
        logger.info("Checking availability for unit {} from {} to {}", unitId,checkIn,checkOut);

        //verify unit exists
        if (!unitRepository.existsById(unitId)){
            throw new ResourceNotFoundException("Unit not found with ID: " +unitId );
        }

        //Check for overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                unitId,checkIn,checkOut
        );

        return overlappingBookings.isEmpty();
    }

    /**
     * Helper method to build BookingResponse from Booking entity
     */
    private BookingResponse buildBookingResponse(Booking booking) {
        long numberOfNights = ChronoUnit.DAYS.between(
                booking.getCheckInDate(),
                booking.getCheckOutDate()
        );

        //Calculate payment totals
        BigDecimal totalPaid  = booking.getPayments().stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.COMPLETED)
                .map(com.staynest.entity.Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingAmount = booking.getTotalPrice().subtract(totalPaid);

        //Build payment response
        List<PaymentResponse> paymentResponses = booking.getPayments().stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getBookingId().toString())
                .unitId(booking.getUnit().getUnitId().toString())
                .unitName(booking.getUnit().getUnitName())
                .unitNumber(booking.getUnit().getUnitNumber())
                .propertyId(booking.getUnit().getProperty().getPropertyId().toString())
                .propertyName(booking.getUnit().getProperty().getPropertyName())
                .propertyCity(booking.getUnit().getProperty().getCity())
                .guestId(booking.getGuest().getUserId().toString())
                .guestName(booking.getGuest().getFullName())
                .guestEmail(booking.getGuest().getEmail())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfNights((int)numberOfNights)
                .numGuests(booking.getNumGuests())
                .specialRequests(booking.getSpecialRequest())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .payments(paymentResponses)
                .totalPaid(totalPaid)
                .remainingAmount(remainingAmount)
                .review(booking.getReview() != null ? buildReviewResponse(booking.getReview()): null)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .discountAmount(booking.getDiscountAmount())
                .finalPrice(booking.getFinalPrice() != null
                        ? booking.getFinalPrice()
                        : booking.getTotalPrice())
                .appliedCouponCode(booking.getBookingCoupons().isEmpty()
                        ? null
                        : booking.getBookingCoupons().get(0).getCouponCodeSnapshot())
                .build();
    }


    /**
     * Helper method to build PaymentResponse from payment entity
     */

    private PaymentResponse buildPaymentResponse(com.staynest.entity.Payment payment){
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId().toString())
                .bookingId(payment.getBooking().getBookingId().toString())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    /**
     * Helper method to build ReviewCreateResponse from Review entity
     */
    private ReviewCreateResponse buildReviewResponse(com.staynest.entity.Review review){
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
