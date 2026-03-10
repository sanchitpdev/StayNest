package com.staynest.service;

import com.staynest.dto.request.ChangePasswordRequest;
import com.staynest.dto.request.UserUpdateRequest;
import com.staynest.dto.response.UserProfileResponse;
import com.staynest.dto.response.UserStatsResponse;
import com.staynest.entity.User;
import com.staynest.enums.UserRole;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for user management operations
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get user profile by ID
     * @param userId - User ID to view
     * @return UserProfileResponse
     */
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(UUID userId){
        logger.info("Fetching profile for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID:" + userId));

        return  buildUserProfileResponse(user);
    }

    /**
     *Update user profile
     * @param userId - User ID
     * @param request - Updated profile data
     * @return UserProfileResponse
     */
    @Transactional
    public UserProfileResponse updateUserProfile(UUID userId, UserUpdateRequest request){
        logger.info("Updating profile for user {}", userId);

        //Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User found with ID: "+ userId));

        //Update fields (Only if provided)
        if (request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null){
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null){
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }

        //Saved updated user
        User updatedUser = userRepository.save(user);
        logger.info("Profile updated for user {}", userId);

        return buildUserProfileResponse(updatedUser);
    }

    /**
     * Change user password
     * @param userId - User ID
     * @param request - Password change request
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request){
        logger.info("Changing password for user {}", userId );

        //Step 1: Validate password match
        if (!request.passwordMatch()){
            throw new BadRequestException("New password and confirmation do not match ");
        }

        //Step 2: Find User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: "+ userId));

        //Step 3: Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())){
            throw new BadRequestException("Current password is incorrect");
        }

        //Step 4: Check new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())){
            throw new BadRequestException("New password must be different from current password");
        }

        //Step 5: Update Password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        logger.info("Password changes successfully for user {}", userId);
    }

    /**
     * Get User statistics
     * @param userId - User ID
     * @return UserStatsResponse
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats(UUID userId){
            logger.info("Fetching statistics for user {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with user ID: " + userId));

            UserStatsResponse.UserStatsResponseBuilder stats = UserStatsResponse.builder();

            if (user.getRole() == UserRole.GUEST || user.getRole() == UserRole.HOST){
                //Guest statistics
                long totalBookings = bookingRepository.findByGuest_UserId(userId).size();
                long upcomingBookings = bookingRepository.findUpcomingBookingsByGuest(
                        userId , java.time.LocalDate.now()
                ).size();

                stats.totalBookings((int) totalBookings)
                        .upcomingBookings((int) upcomingBookings)
                        .reviewWritten(reviewRepository.findByReviewer_UserId(userId).size())
                        .savedProperties(wishlistRepository.findByUser_UserId(userId).size());
            }
            if (user.getRole() == UserRole.HOST){
                //Host statistics
                long totalProperties = propertyRepository.countByHost_UserId(userId);
                long reviewsReceived = reviewRepository.findReviewsForHostProperties(userId).size();

                stats.totalProperties((int) totalProperties)
                        .reviewsReceived((int) reviewsReceived);
            }
            return stats.build();
    }

    /**
     * Helper method to build UserProfileResponse from User entity
     */
    private UserProfileResponse buildUserProfileResponse(User user) {
        //Calculate statistics
        int totalBookings = bookingRepository.findByGuest_UserId(user.getUserId()).size();
        int totalProperties = (int) propertyRepository.countByHost_UserId(user.getUserId());
        int totalReviews = reviewRepository.findByReviewer_UserId(user.getUserId()).size();

        return UserProfileResponse.builder()
                .userId(user.getUserId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role(user.getRole().name())
                .isVerified(user.getIsVerified())
                .totalBookings(totalBookings)
                .totalProperties(totalProperties)
                .totalReviews(totalReviews)
                .memberSince(user.getCreatedAt())
                .build();
    }
}
