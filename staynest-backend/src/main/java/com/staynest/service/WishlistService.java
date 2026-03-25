package com.staynest.service;

import com.staynest.dto.response.PagedResponse;
import com.staynest.dto.response.WishlistResponse;
import com.staynest.entity.Property;
import com.staynest.entity.User;
import com.staynest.entity.Wishlist;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for wishlist management operations
 */
@Service
public class WishlistService {

    private static final Logger logger = LoggerFactory.getLogger(WishlistService.class);

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Add property to wishlist

     * @param propertyId - Property to save
     * @param userID - User saving the property
     * @return WishlistResponse
     */
    @Transactional
    public WishlistResponse addWishlist(UUID propertyId, UUID userID){
        logger.info("Adding property {} to wishlist for user {}", propertyId, userID);

        //Step 1: Find User
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with Id: " + userID));

        //Step 2: Find Property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("property not found with Id: "+ propertyId));

        //Step 3: Check if already in wishList
        if (wishlistRepository.existsByUser_UserIdAndProperty_PropertyId(userID, propertyId)){
            throw new BadRequestException("Property is already in your wishlist");
        }

        //Step 4: Create wishlist entry
        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .property(property)
                .build();

        //Step 5: Save wishlist
        Wishlist savedWishlist = wishlistRepository.save(wishlist);
        logger.info("Property {} added to wishlist for user {}",propertyId,userID);

        //Step 6: Build and return response
        return buildWishlistResponse(savedWishlist);
        
    }

    /**
     * Remove property from wishlist
     * @param propertyId - Property to remove
     * @param userId - User removing the property
     */
    @Transactional
    public void removeFromWishlist(UUID propertyId, UUID userId){
        logger.info("Removing property {} from wishlist for user {}", propertyId, userId);

        //Find wishlist entry
        Wishlist wishlist = wishlistRepository.findByUser_UserIdAndProperty_PropertyId(userId, propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found in your wishlist"));

        //Delete wishlist entry
        wishlistRepository.delete(wishlist);
        logger.info("Property {} removed form wishlist for user {}", propertyId,userId);
    }

    /**
     * Get all wishlist for a user
     * @param userId - User id
     * @param page - Page no
     * @param size - Page Size
     * @return - List of WishlistResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<WishlistResponse> getMyWishlist(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Wishlist> wishlistPage = wishlistRepository.findByUser_UserId(userId, pageable);
        Page<WishlistResponse> responsePage = wishlistPage.map(this::buildWishlistResponse);
        return PagedResponse.of(responsePage);
    }

    /**
     * Check if property is in user's wishlist.
     * @param propertyId - PropertyId
     * @param userId - User Id
     * @return true if saved, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isPropertySaved(UUID propertyId, UUID userId){
        return wishlistRepository.existsByUser_UserIdAndProperty_PropertyId(userId, propertyId);
    }

    /**
     * Get wishlist count for user
     * @param userId - User Id
     * @return Number of saved properties
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(UUID userId){
        return wishlistRepository.findByUser_UserId(userId).size();
    }

    /**
     * Helper method to build wishlistResponse from wishlist entity
     */
    private WishlistResponse buildWishlistResponse(Wishlist wishlist) {

        Property property = wishlist.getProperty();

        //Calculate statics
        int totalUnits = (int) unitRepository.countByProperty_PropertyId(property.getPropertyId());
        int availableUnits = unitRepository.findByProperty_PropertyIdAndIsAvailable(
                property.getPropertyId(), true
        ).size();

        //Get the lowest unit price
        BigDecimal startingPrice = property.getUnits().stream()
                .map(unit -> unit.getBasePrice())
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        //Get review statistics
        Double averageRating = reviewRepository.calculateAverageRating(property.getPropertyId());
        Integer reviewCount = (int) reviewRepository.countByProperty_PropertyId(property.getPropertyId());

        return WishlistResponse.builder()
                .wishlistId(wishlist.getWishlistId().toString())
                .propertyId(property.getPropertyId().toString())
                .propertyName(property.getPropertyName())
                .propertyType(property.getPropertyType().name())
                .city(property.getCity())
                .state(property.getState())
                .country(property.getCountry())
                .hostId(property.getHost().getUserId().toString())
                .hostName(property.getHost().getFullName())
                .startingPrice(startingPrice)
                .totalUnits(totalUnits)
                .availableUnits(availableUnits)
                .averageRating(averageRating)
                .reviewCount(reviewCount)
                .savedAt(wishlist.getCreatedAt())
                .build();
    }
}
