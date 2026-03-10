package com.staynest.controller;

import com.staynest.dto.response.WishlistResponse;
import com.staynest.entity.User;
import com.staynest.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for wishlist management
 */
@RestController
@RequestMapping("/whishlists")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    /**
     * Add property to wishlist
     */
    @PostMapping("/{propertyId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @PathVariable UUID propertyId,
            Authentication authentication
            ){
        User user = (User) authentication.getPrincipal();
        WishlistResponse response = wishlistService.addWishlist(propertyId,user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Remove property form wishlist
     */
    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable UUID propertyId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        wishlistService.removeFromWishlist(propertyId, user.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get my wishlists (saved properties)
     */
    @GetMapping("/my-wishlists")
    public ResponseEntity<List<WishlistResponse>> getMyWishlists(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<WishlistResponse> wishlist = wishlistService.getMyWishlist(user.getUserId());
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Check if property is saved
     */
    @GetMapping("/is-saved/{propertyId}")
    public ResponseEntity<Map<String, Boolean>> isPropertySaved(
            @PathVariable UUID propertyId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        boolean isSaved  = wishlistService.isPropertySaved(propertyId, user.getUserId());
        return ResponseEntity.ok(Map.of("isSaved",isSaved));
    }

    /**
     * Get wishlist count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String,Long>> getWishlistCount(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        long count = wishlistService.getWishlistCount(user.getUserId());
        return ResponseEntity.ok(Map.of("count",count));
    }
}
