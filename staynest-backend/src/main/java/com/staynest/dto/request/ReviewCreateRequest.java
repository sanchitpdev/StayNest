package com.staynest.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a review
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {

    @NotNull(message = "Booking Id is required")
    private UUID bookingId;

    @NotNull(message = "Rating is required")
    @Min(value = 1,message = "Rating must be atleast 1")
    @Max(value = 5,message = "Rating must be at most 5")
    private Integer rating;

    @Size(max = 2000,message = "Comments cannot exceed 2000 characters")
    private String comment;

    //Category Rating
    @Min(1) @Max(5)
    private Integer cleanlinessRating;

    @Min(1) @Max(5)
    private Integer accuracyRating;

    @Min(1) @Max(5)
    private Integer communicationRating;

    @Min(1) @Max(5)
    private Integer locationRating;

    @Min(1) @Max(5)
    private Integer valueRating;


}
