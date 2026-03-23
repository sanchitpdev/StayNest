package com.staynest.dto.response;

import com.staynest.enums.CancellationPolicy;
import com.staynest.enums.PropertyStatus;
import com.staynest.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**'
 * DTO for property response
 * Returned when retrieved property details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyResponse {

    private String propertyId;
    private String propertyName;
    private String description;
    private PropertyType propertyType;

    //Address
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private BigDecimal latitude;
    private BigDecimal longitude;

    //Amenities
    private Map<String, Object> amenities;

    //Host Information
    private String hostId;
    private String hostName;
    private String hostEmail;

    //Statistics
    private Integer totalUnits;
    private Integer availableUnits;
    private BigDecimal startingPrice;//Lowest Unit price

    //TimeStamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //Optional: List of Units
    private List<UnitCreateResponse> units;

    private PropertyStatus propertyStatus;
    private CancellationPolicy cancellationPolicy;
    private Integer minStayNights;

}
