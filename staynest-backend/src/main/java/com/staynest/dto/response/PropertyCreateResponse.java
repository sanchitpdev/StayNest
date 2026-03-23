    package com.staynest.dto.response;

    import com.staynest.enums.PropertyType;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.Map;
    import java.util.Objects;

    /**
     * DTO for property creation response
     * Returned immediately after a property
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class PropertyCreateResponse {

        private String propertyId;
        private String propertyName;
        private String description;
        private PropertyType propertyType;
        private String address;
        private String city;
        private String state;
        private String country;
        private String postalCode;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Map<String, Object> amenities;
        private String hostId;
        private LocalDateTime createdAt;
    }
