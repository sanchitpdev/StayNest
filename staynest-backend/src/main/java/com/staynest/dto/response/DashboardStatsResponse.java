package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO for dashboard statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsResponse {

    //User-Specific stats
    private String userId;
    private String userRole;

    //Guest Dashboard
    private Integer totalBookings;
    private Integer upcomingBookings;
    private Integer pastBookings;
    private Integer cancelledBookings;
    private BigDecimal totalSpent;
    private Integer reviewsWritten;
    private Integer savedProperties;

    //Host DashBoard
    private Integer totalProperties;
    private Integer totalUnits;
    private Integer activeListings;
    private Integer totalBookingsReceived;
    private Integer upcomingBookingsReceived;
    private BigDecimal totalEarnings;
    private BigDecimal pendingPayments;
    private Integer totalReviewsReceived;
    private Double averageRating;
    private Integer totalGuests;

    //Activity breakdown (last 30days)
    private Map<String, Integer> bookingsByMonth;
    private Map<String, BigDecimal> earningsByMonth;

    //Top performing properties (for host)
    private java.util.List<TopPropertyStats> topProperties;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopPropertyStats{
        private String propertyId;
        private String propertyName;
        private Integer totalBookings;
        private BigDecimal totalEarnings;
        private Double averageRating;
    }
}
