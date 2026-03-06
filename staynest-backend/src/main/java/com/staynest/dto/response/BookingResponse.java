package com.staynest.dto.response;

import com.staynest.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for cleat booking response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private String bookingId;

    //Unit Information
    private String unitId;
    private String unitName;
    private String unitNumber;

    //Property Information
    private String propertyId;
    private String propertyName;
    private String propertyCity;

    //Guest Information
    private String guestId;
    private String guestName;
    private String guestEmail;

    //Booking details
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfNights;
    private Integer numGuests;
    private String specialRequests;

    //Pricing
    private BigDecimal totalPrice;
    private BookingStatus bookingStatus;

    //Payment Information
    private List<PaymentResponse> payments;
    private BigDecimal totalPaid;
    private BigDecimal remainingAmount;

    //Review (if exists)
    private ReviewCreateResponse review;

    //TimeStamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
