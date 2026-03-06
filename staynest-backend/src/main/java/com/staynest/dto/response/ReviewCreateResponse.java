package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for review response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateResponse {

    private String reviewId;
    private String bookingId;
    private String propertyId;
    private String propertyName;
    private String reviewerId;
    private String reviewerName;

    private Integer rating;
    private String comment;

    //Category Rating
    private Integer cleanlinessRating;
    private Integer accuracyRating;
    private Integer communicationRating;
    private Integer locationRating;
    private Integer valueRating;
    private LocalDateTime createdAt;
}
