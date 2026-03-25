package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingResponse {
    private String unitId;
    private String unitName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfNights;
    private BigDecimal basePricePerNight;
    private BigDecimal cleaningFee;
    private BigDecimal totalBasePrice;
    private BigDecimal totalPrice;
    private Map<LocalDate, BigDecimal> dailyPrices; // date → price breakdown
}