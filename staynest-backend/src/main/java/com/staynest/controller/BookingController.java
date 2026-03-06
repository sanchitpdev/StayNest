package com.staynest.controller;

import com.staynest.dto.request.BookingCreateRequest;
import com.staynest.dto.response.BookingCreateResponse;
import com.staynest.dto.response.BookingResponse;
import com.staynest.entity.User;
import com.staynest.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for booking management
 * Handles all bookings-related operations
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /**
     * Create new booking
     * Guest book a unit for specific dates.
     * POST /bookings
     */
    @PostMapping
    public ResponseEntity<BookingCreateResponse> createBooking(
            @Valid @RequestBody BookingCreateRequest request,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        BookingCreateResponse response = bookingService.createBooking(request,user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get Booking by ID
     * Only guest or property host can view.
     * GET /api/v1/bookings/{bookingId}
     *
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(
            @PathVariable UUID bookingId,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        BookingResponse response = bookingService.getBookingById(bookingId,user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all my bookings (as guest)

     * GET /api/v1/bookings/my-bookings
     */
    @GetMapping("/my-bookings")
    public ResponseEntity<List<BookingResponse>> getMyBookings(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<BookingResponse> bookings = bookingService.getMyBookings(user.getUserId());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get upcoming bookings(as guest)

     * GET /api/v1/bookings/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<BookingResponse>> getUpcomingBookings(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<BookingResponse> bookings = bookingService.getUpcomingBookings(user.getUserId());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Get all bookings for property (as host)
     * GET api/v1/bookings/property/{propertyId}
     */
    @GetMapping("property/{propertyId}")
    public ResponseEntity<List<BookingResponse>> getPropertyBookings(
            @PathVariable UUID propertyId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        List<BookingResponse> bookings = bookingService.getPropertyBooking(propertyId, user.getUserId());
        return ResponseEntity.ok(bookings);
    }

    /**
     * Cancel a booking
     * Only guest can cancel their own bookings

     * POST /api/v1/bookings/{bookingId}/cancle
     */
    @PostMapping("{bookingId}/cancel")
    public ResponseEntity<Void> cancelBooking(
            @PathVariable UUID bookingId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        bookingService.cancelBooking(bookingId,user.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Confirm a booking (host approves)
     * Only property host can confirm

     * POST api/v1/bookings/{bookingId}/confirm
     */
    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(
            @PathVariable UUID bookingId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        BookingResponse response = bookingService.confirmBooking(bookingId,user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Check unit availability for given dates
     * Public endpoint anyone can check

     * GET /api/v1/bookings/availibilty/{unitId}?checkIn=2026-03-15&checkOut=2026-03-20
     */
    @GetMapping("/availability/{unitId}")
    public ResponseEntity<java.util.Map<String,Boolean>> checkAvailability(
            @PathVariable UUID unitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate checkOut
            ){
        boolean available = bookingService.checkAvailability(unitId,checkIn,checkOut);
        return ResponseEntity.ok(java.util.Map.of("available",available));
    }

}
