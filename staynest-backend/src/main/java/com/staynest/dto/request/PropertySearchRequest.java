package com.staynest.dto.request;

import com.staynest.enums.PropertyType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for advance property search
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertySearchRequest {

    //Location filter
    private String city;
    private String state;
    private String country;

    //Property type filter
    private PropertyType propertyType;

    //Pricing filter
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    //Capacity filter
    private Integer minBedrooms;
    private Integer maxBedrooms;
    private Integer minGuests;

    //Date availability filter
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    //Amenities filter (comma-separated : "wifi,pool,parking")
    private String amenities;

    //Rating filter
    private Double minRating;

    //Sorting
    private String sortBy;
    private String sortDirection;

    //Pagination
    private Integer page;
    private Integer size;

    /**
     * Get page number (default 0)
     */
    public int getPageNumber(){
        return page != null && page >= 0 ? page : 0;
    }

    /**
     * Get page size (default 10 , max 100)
     */
    public int getPageSize(){
        if (size == null || size <= 0){
            return 10;
        }
        return Math.min(size,100);
    }

}
