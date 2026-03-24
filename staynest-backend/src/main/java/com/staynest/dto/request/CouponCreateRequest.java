package com.staynest.dto.request;

import com.staynest.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {

    @NotBlank(message = "Coupon code is required")
    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be greater than 0")
    private BigDecimal discountValue;

    private BigDecimal minBookingAmount;
    private BigDecimal maxDiscountAmount;

    @NotNull(message = "Valid from date is required")
    @FutureOrPresent(message = "Valid from must be today or future")
    private LocalDate validFrom;

    @NotNull(message = "Valid until date is required")
    @Future(message = "Valid until must be a future date")
    private LocalDate validUntil;

    private Integer maxUsageLimit;

    @Min(value = 1, message = "Per user limit must be at least 1")
    private Integer perUserLimit = 1;
}