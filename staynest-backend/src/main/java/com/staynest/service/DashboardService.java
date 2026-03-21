package com.staynest.service;

import com.staynest.dto.response.DashboardStatsResponse;
import com.staynest.entity.Booking;
import com.staynest.entity.Property;
import com.staynest.entity.User;
import com.staynest.enums.BookingStatus;
import com.staynest.enums.UserRole;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for dashboard statistics and analytics.
 * Provides comprehensive data for guest and host dashboards.
 */
@Service
public class DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    /**
     * Get comprehensive dashboard statistics for a user.
     * Returns different stats based on user role (GUEST vs HOST).
     *
     * @param userId - User ID
     * @return DashboardStatsResponse
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(UUID userId) {
        logger.info("Fetching dashboard statistics for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        DashboardStatsResponse.DashboardStatsResponseBuilder stats = DashboardStatsResponse.builder()
                .userId(user.getUserId().toString())
                .userRole(user.getRole().name());

        // Common stats for all users
        addGuestStats(stats, userId);

        // Host-specific stats
        if (user.getRole() == UserRole.HOST) {
            addHostStats(stats, userId);
        }

        return stats.build();
    }

    /**
     * Add guest-specific statistics.
     */
    private void addGuestStats(DashboardStatsResponse.DashboardStatsResponseBuilder stats, UUID userId) {
        // Get all bookings
        List<Booking> allBookings = bookingRepository.findByGuest_UserId(userId);

        // Total bookings
        int totalBookings = allBookings.size();

        // Upcoming bookings
        int upcomingBookings = (int) allBookings.stream()
                .filter(b -> b.getCheckInDate().isAfter(LocalDate.now()))
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        // Past bookings
        int pastBookings = (int) allBookings.stream()
                .filter(b -> b.getCheckOutDate().isBefore(LocalDate.now()))
                .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                .count();

        // Cancelled bookings
        int cancelledBookings = (int) allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CANCELLED)
                .count();

        // Total spent (calculate from completed bookings)
        BigDecimal totalSpent = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED ||
                        b.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Reviews written
        int reviewsWritten = reviewRepository.findByReviewer_UserId(userId).size();

        // Saved properties
        int savedProperties = wishlistRepository.findByUser_UserId(userId).size();

        // Bookings by month (last 6 months)
        Map<String, Integer> bookingsByMonth = calculateBookingsByMonth(allBookings);

        stats.totalBookings(totalBookings)
                .upcomingBookings(upcomingBookings)
                .pastBookings(pastBookings)
                .cancelledBookings(cancelledBookings)
                .totalSpent(totalSpent)
                .reviewsWritten(reviewsWritten)
                .savedProperties(savedProperties)
                .bookingsByMonth(bookingsByMonth);
    }

    /**
     * Add host-specific statistics.
     */
    private void addHostStats(DashboardStatsResponse.DashboardStatsResponseBuilder stats, UUID userId) {
        // Get all properties
        List<Property> properties = propertyRepository.findByHost_UserId(userId);
        int totalProperties = properties.size();

        // Total units
        int totalUnits = (int) properties.stream()
                .mapToLong(p -> unitRepository.countByProperty_PropertyId(p.getPropertyId()))
                .sum();

        // Active listings (properties with available units)
        int activeListings = (int) properties.stream()
                .filter(p -> unitRepository.findByProperty_PropertyIdAndIsAvailable(p.getPropertyId(), true).size() > 0)
                .count();

        // Get all bookings for host's properties
        List<Booking> allBookings = properties.stream()
                .flatMap(p -> p.getUnits().stream())
                .flatMap(u -> u.getBookings().stream())
                .collect(Collectors.toList());

        int totalBookingsReceived = allBookings.size();

        // Upcoming bookings received
        int upcomingBookingsReceived = (int) allBookings.stream()
                .filter(b -> b.getCheckInDate().isAfter(LocalDate.now()))
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .count();

        // Total earnings (from completed bookings)
        BigDecimal totalEarnings = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Pending payments (confirmed but not completed)
        BigDecimal pendingPayments = allBookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Reviews received
        int totalReviewsReceived = (int) properties.stream()
                .mapToLong(p -> reviewRepository.countByProperty_PropertyId(p.getPropertyId()))
                .sum();

        // Average rating across all properties
        Double averageRating = properties.stream()
                .map(p -> reviewRepository.calculateAverageRating(p.getPropertyId()))
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Total unique guests
        int totalGuests = (int) allBookings.stream()
                .map(b -> b.getGuest().getUserId())
                .distinct()
                .count();

        // Earnings by month
        Map<String, BigDecimal> earningsByMonth = calculateEarningsByMonth(allBookings);

        // Top performing properties (top 5)
        List<DashboardStatsResponse.TopPropertyStats> topProperties = calculateTopProperties(properties);

        stats.totalProperties(totalProperties)
                .totalUnits(totalUnits)
                .activeListings(activeListings)
                .totalBookingsReceived(totalBookingsReceived)
                .upcomingBookingsReceived(upcomingBookingsReceived)
                .totalEarnings(totalEarnings)
                .pendingPayments(pendingPayments)
                .totalReviewsReceived(totalReviewsReceived)
                .averageRating(averageRating)
                .totalGuests(totalGuests)
                .earningsByMonth(earningsByMonth)
                .topProperties(topProperties);
    }

    /**
     * Calculate bookings by month for the last 6 months.
     */
    private Map<String, Integer> calculateBookingsByMonth(List<Booking> bookings) {
        Map<String, Integer> result = new LinkedHashMap<>();

        // Get last 6 months
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            String monthKey = month.toString(); // Format: "2026-03"

            long count = bookings.stream()
                    .filter(b -> {
                        YearMonth bookingMonth = YearMonth.from(b.getCreatedAt());
                        return bookingMonth.equals(month);
                    })
                    .count();

            result.put(monthKey, (int) count);
        }

        return result;
    }

    /**
     * Calculate earnings by month for the last 6 months.
     */
    private Map<String, BigDecimal> calculateEarningsByMonth(List<Booking> bookings) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();

        // Get last 6 months
        for (int i = 5; i >= 0; i--) {
            YearMonth month = YearMonth.now().minusMonths(i);
            String monthKey = month.toString();

            BigDecimal earnings = bookings.stream()
                    .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                    .filter(b -> {
                        YearMonth bookingMonth = YearMonth.from(b.getCreatedAt());
                        return bookingMonth.equals(month);
                    })
                    .map(Booking::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.put(monthKey, earnings);
        }

        return result;
    }

    /**
     * Calculate top 5 performing properties.
     */
    private List<DashboardStatsResponse.TopPropertyStats> calculateTopProperties(List<Property> properties) {
        return properties.stream()
                .map(property -> {
                    // Get all bookings for this property
                    List<Booking> propertyBookings = property.getUnits().stream()
                            .flatMap(u -> u.getBookings().stream())
                            .collect(Collectors.toList());

                    int totalBookings = propertyBookings.size();

                    BigDecimal totalEarnings = propertyBookings.stream()
                            .filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED)
                            .map(Booking::getTotalPrice)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Double averageRating = reviewRepository.calculateAverageRating(property.getPropertyId());

                    return DashboardStatsResponse.TopPropertyStats.builder()
                            .propertyId(property.getPropertyId().toString())
                            .propertyName(property.getPropertyName())
                            .totalBookings(totalBookings)
                            .totalEarnings(totalEarnings)
                            .averageRating(averageRating != null ? averageRating : 0.0)
                            .build();
                })
                .sorted((a, b) -> b.getTotalEarnings().compareTo(a.getTotalEarnings()))
                .limit(5)
                .collect(Collectors.toList());
    }
}