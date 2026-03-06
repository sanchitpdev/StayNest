package com.staynest.dto.request;

import com.staynest.enums.PropertyType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * DTO to Create a new Property
 * Used when HOST create a new Property
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyCreateRequest {

    @NotBlank(message = "Property name is required")
    @Size(min = 5,max = 200,message = "Property name must be between 5 to 200 characters")
    private String propertyName;

    @NotBlank(message = "Description is required")
    @Size(min = 20,max = 500,message = "Description must be between 20 to 500 characters")
    private String description;

    @NotNull(message = "Property type is required")
    private PropertyType propertyType;

    //Address fields
    @NotBlank(message = "Address is requires")
    @Size(max = 500, message = "Address cannot excess 500 characters")
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;

    @Size(max = 20,message = "Postal code cannot exceeds 20 characters")
    private String postalCode;

    //Optional location Co-ordinates
    @DecimalMin(value = "-90.0",message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0",message = "Latitude must be between -90 and 90")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0",message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0",message = "Longitude must be between -180 and 180")
    private BigDecimal longitude;

    //Amenities as JSON map(e.g,{"Wi-Fi": true ,"parking": true, "pool": false})
    private Map<String,Object> amenities;



}
