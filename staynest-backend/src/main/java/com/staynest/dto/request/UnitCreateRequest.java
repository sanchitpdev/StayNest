package com.staynest.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating new unit within property
 * A property have multiple units (e.g., different rooms in hotel)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitCreateRequest {

    @NotNull(message = "Property ID is required")
    private UUID propertyId;

    @NotBlank(message = "Unit name is required")
    @Size(min = 2,max = 100,message = "Unit name must be between 2 to 200 charachters" )
    private String unitName;

    @NotBlank(message = "Unit number is requires")
    @Size(max = 50,message = "Unit number cannot exceed 50 charachter")
    private String unitNumber;

    @NotNull(message = "Number of bedrooms are required")
    @Min(value = 0,message = "Bedrooms cannot be negative")
    @Max(value = 20,message = "Bedrooms cannot exceeds 20")
    private Integer bedrooms;

    @NotNull(message = "Number of bathrooms are required")
    @DecimalMin(value = "0.5",message = "Bathrooms must be at least 0.5")
    @DecimalMax(value = "20.0",message = "Bathrooms cannot exceeds 20")
    private BigDecimal bathrooms;

    @NotNull(message = "Maximum guest are required")
    @Min(value = 1,message = "Must accomadate at least 1 guest ")
    @Max(value = 50,message = "Can not exceeds 50 guests")
    private Integer maxGuests;

    @Min(value = 0,message = "Square feet cannot be negative")
    private Integer squareFeet;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.01",message = "Base price must be greater than 0")
    @Digits(integer = 8,fraction = 2,message = "Invalid price format")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0",message = "Cleaning fee cannot be negative")
    @Digits(integer = 6,fraction = 2,message = "Invalid cleaning fee format")
    private BigDecimal cleaningFee;

    private Boolean isAvailable = true;

}
