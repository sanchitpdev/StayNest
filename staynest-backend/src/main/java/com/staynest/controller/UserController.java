package com.staynest.controller;

import com.staynest.dto.request.ChangePasswordRequest;
import com.staynest.dto.request.UserUpdateRequest;
import com.staynest.dto.response.ApiResponse;
import com.staynest.dto.response.MessageResponse;
import com.staynest.dto.response.UserProfileResponse;
import com.staynest.dto.response.UserStatsResponse;
import com.staynest.entity.User;
import com.staynest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get my profile
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        UserProfileResponse profile = userService.getUserProfile(user.getUserId());
        return ResponseEntity.ok(profile);
    }

    /**
     * Get user profile bu Id (public)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable UUID userId){
        UserProfileResponse profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    /**
     * Update my profile
     */
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        UserProfileResponse profile = userService.updateUserProfile(user.getUserId(), request);
        return ResponseEntity.ok(profile);
    }

    /**
     * Change Password
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<ApiResponse> changPassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        userService.changePassword(user.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password change successfully"));
    }

    /**
     * Get my statistics
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getMyStatus(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        UserStatsResponse stats = userService.getUserStats(user.getUserId());
        return ResponseEntity.ok(stats);
    }
}
