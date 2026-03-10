package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for user statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatsResponse {

    //Guest statistics
    private Integer totalBookings;
    private Integer upcomingBookings;
    private Integer completeBookings;
    private Integer cancelledBookings;
    private BigDecimal totalSpent;
    private Integer reviewWritten;
    private Integer savedProperties;

    //Host Statistics
    private Integer totalProperties;
    private Integer totalUnits;
    private Integer totalBookingReceived;
    private BigDecimal totalEarnings;
    private Integer reviewsReceived;
    private Double averageRating;
}
