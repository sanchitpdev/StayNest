package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for unit response
 * Returned when creating or retrieving unit details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitCreateResponse {

    private String unitId;
    private String propertyId;
    private String unitName;
    private String unitNumber;
    private Integer bedrooms;
    private BigDecimal bathrooms;
    private Integer maxGuests;
    private Integer squareFeet;
    private BigDecimal basePrice;
    private BigDecimal cleaningFee;
    private BigDecimal totalPrice;//Base price + Cleaning fee
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
