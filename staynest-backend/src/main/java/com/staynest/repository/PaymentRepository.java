package com.staynest.repository;

import com.staynest.entity.Payment;
import com.staynest.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    //Find all Payment for a booking
    List<Payment> findByBooking_BookingId(UUID bookingId);

    //Find Payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);

    //Find payment by status
    List<Payment> findByPaymentStatus(PaymentStatus status);

    //Find successful payment for a booking
    List<Payment> findByBooking_BookingIdAndPaymentStatus(
            UUID bookingId,
            PaymentStatus status
    );

    //Calculate total paid amount for a booking
    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p "+
            "WHERE p.booking.bookingId = :bookingId "+
            "AND p.paymentStatus = 'COMPLETED'")
    BigDecimal calculateTotalPaidForBooking(@Param("bookingId") UUID bookingId);

    //Find all Payment for properties owned by a host
    @Query("SELECT p FROM Payment p "+
            "WHERE p.booking.unit.property.host.userId = :hostId " +
            "AND p.paymentStatus = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByHost(@Param("hostId") UUID hostId);

    //Count pending payment
    long countByPaymentStatus(PaymentStatus status);

    }
