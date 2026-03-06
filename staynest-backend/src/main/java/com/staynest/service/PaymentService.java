package com.staynest.service;

import com.staynest.dto.request.PaymentCreateRequest;
import com.staynest.dto.response.PaymentResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.Payment;
import com.staynest.enums.BookingStatus;
import com.staynest.enums.PaymentStatus;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.BookingRepository;
import com.staynest.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for payment management operation
 */
@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public PaymentResponse createPayment(PaymentCreateRequest request, UUID userId){
        logger.info("Creating payment for booking {} by user {}", request.getBookingId(),userId);

        //Step 1: Find the booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: "+ request.getBookingId()));

        //Step 2: Check authorization (Only guest can pay for their booking )
        if (!booking.getGuest().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only make payments for your own bookings");
        }

        //Step 3: Check booking status ( can't pay for cancelled bookings)
        if (booking.getBookingStatus() == BookingStatus.CANCELLED){
            throw new BadRequestException("Cannot make payment for cancelled booking");
        }

        //Step 4: Calculate total already paid
        BigDecimal totalPaid = paymentRepository.calculateTotalPaidForBooking(request.getBookingId());

        //Step 5: Check if payment amount is valid
        BigDecimal remainingAmount = booking.getTotalPrice().subtract(totalPaid);
        logger.debug("Remaining amount: ${}", remainingAmount);

        // ⭐ CRITICAL VALIDATION: Reject overpayment
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new BadRequestException(
                    String.format("Payment amount ($%.2f) exceeds remaining balance ($%.2f)",
                            request.getAmount(), remainingAmount)
            );
        }

        //Step 6: Generate transaction ID
        String transactionId = request.getTransactionId() != null
                ? request.getTransactionId()
                : "TXN_" + UUID.randomUUID().toString().substring(0,8).toUpperCase();

        //Step 7: Create Payment entity
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId(transactionId)
                .paymentDate(LocalDateTime.now())
                .build();

        //Step 8: Save Payment
        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment created successfully with ID: {}", savedPayment.getPaymentId());

        //Step 9: If fully paid, update booking status to CONFIRMED
        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(booking.getTotalPrice()) >= 0){
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            logger.info("Booking {} fully paid and confirmed ", booking.getBookingId());
        }
        //Step 10: Build and return response
        return buildPaymentResponse(savedPayment);
    }

    /**
     * Get Payment by ID
     * @param paymentId - PaymentID
     * @param userId - User requesting (for authorization)
     * @return PaymentResponse
     */
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId,UUID userId){
        logger.info("Fetching payment {} for user {}", paymentId,userId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));

        //Check authorization: Only guest or host can view payment
        if (! payment.getBooking().getGuest().getUserId().equals(userId) &&
                !payment.getBooking().getUnit().getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only view payments for your own bookings");
        }

        return buildPaymentResponse(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentByBooking(UUID bookingId, UUID userId){
        logger.info("Fetching payments for booking {} by user {}",bookingId,userId);

        //Verify booking exists and user is unauthorized
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: "+ bookingId));

        if (!booking.getGuest().getUserId().equals(userId) &&
                !booking.getUnit().getProperty().getHost().getUserId().equals(userId)){
            throw new UnauthorizedException("You can only view payments for your own bookings");
        }

        List<Payment> payments = paymentRepository.findByBooking_BookingId(bookingId);
        return payments.stream()
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all payments made by user(guest view)
     * @param userId - Guest user id
     * @return list of PaymentResponse
     */
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(UUID userId){
        logger.info("Fetching payments for user {}", userId);

        //Get all bookings by user, then get payments
        List<Booking> bookings = bookingRepository.findByGuest_UserId(userId);

        return bookings.stream()
                .flatMap(booking -> booking.getPayments().stream())
                .map(this::buildPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to build PaymentResponse from Payment entity.
     */
    private PaymentResponse buildPaymentResponse(Payment payment) {
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
}
