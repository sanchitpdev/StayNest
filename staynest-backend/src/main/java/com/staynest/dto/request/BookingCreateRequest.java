package com.staynest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for Creating a new booking
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreateRequest {

    @NotNull(message = "Unit ID is required")
    private UUID unitId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check in date must be future or today")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is reuired")
    @Min(value = 1,message = "At least 1 guest is required")
    @Max(value = 50, message = "cannot exceed 50 guests")
    private Integer numGuests;

    @Size(max = 1000,message = "Special request cannot exceed 1000 characters")
    private String specialRequest;

    //Checkout must be after check-In date
    public boolean isValidDateRange(){
        if (checkInDate == null || checkOutDate == null){
            return false;
        }
        return checkOutDate.isAfter(checkInDate);
    }

}
