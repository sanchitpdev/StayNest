package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for wishlist response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {

    private String wishlistId;

    //Property information
    private String propertyId;
    private String propertyName;
    private String propertyType;
    private String city;
    private String state;
    private String country;

    //Host information
    private String hostId;
    private String hostName;

    //Pricing information
    private BigDecimal startingPrice;
    private Integer totalUnits;
    private Integer availableUnits;

    //Rating
    private Double averageRating;
    private Integer reviewCount;

    //TimeStamp
    private LocalDateTime savedAt;

}
