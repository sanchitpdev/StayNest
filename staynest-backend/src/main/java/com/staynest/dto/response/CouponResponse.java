package com.staynest.dto.response;

import com.staynest.enums.CouponStatus;
import com.staynest.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private String couponId;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minBookingAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Integer maxUsageLimit;
    private Integer perUserLimit;
    private Integer usageCount;
    private CouponStatus couponStatus;
    private LocalDateTime createdAt;
}