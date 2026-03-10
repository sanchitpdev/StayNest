package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user profile response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String profilePictureUrl;
    private String role;
    private Boolean isVerified;

    //Statistics
    private Integer totalBookings;
    private Integer totalProperties;
    private Integer totalReviews;

    private LocalDateTime memberSince;
}
