package com.staynest.repository;

import com.staynest.entity.Booking;
import com.staynest.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking,UUID> {

    //Find all bookings by guest
    List<Booking> findByGuest_UserId(UUID guestId);

    //find all bookings by units
    List<Booking> findByUnit_UnitId(UUID unitId);

    //Find booking by status
    List<Booking> findByBookingStatus(BookingStatus bookingStatus);

    //Find guest booking by  status
    List<Booking> findByGuest_UserIdAndBookingStatus(UUID guestId,BookingStatus bookingStatus);

    //Find upcoming booking for a guest
    @Query("SELECT b FROM Booking b "+
            "WHERE b.guest.userId = :guestId "+
            "AND b.checkInDate >= :today "+
            "AND b.bookingStatus = 'CONFIRMED' "+
            "ORDER BY b.checkInDate ASC")
    List<Booking> findUpcomingBookingsByGuest(
            @Param("guestId") UUID guestId,
            @Param("today") LocalDate today
            );

    //Check for overlapping booking (prevent double booking)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.unit.unitId = :unitId "+
            "AND b.bookingStatus IN ('PENDING','CONFIRMED') "+
            "AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)")
    List<Booking> findOverlappingBookings(
            @Param("unitId") UUID unitId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );

    //Find booking for property (through units)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.unit.property.propertyId= :propertyId")
    List<Booking> findByPropertyId(@Param("propertyId") UUID propertyId);

    //Find booking that can be reviewed(Completed and after checkout)
    @Query("SELECT b FROM Booking b "+
            "WHERE b.guest.userId= :guestId " +
            "AND b.bookingStatus = 'COMPLETED' " +
            "AND b.checkOutDate < :today " +
            "AND b.review IS NULL")
    List<Booking> findReviewableBookings(
            @Param("guestId") UUID guestId,
            @Param("today") LocalDate today
    );
}
