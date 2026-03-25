package com.staynest.service;

import com.staynest.dto.response.PricingResponse;
import com.staynest.entity.Unit;
import com.staynest.entity.UnitPricing;
import com.staynest.enums.PricingType;
import com.staynest.exception.BadRequestException;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.repository.UnitPricingRepository;
import com.staynest.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PricingService {

    private static final Logger logger = LoggerFactory.getLogger(PricingService.class);

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private UnitPricingRepository unitPricingRepository;

    /**
     * Calculate price for a date range.
     * Checks for special pricing rules (weekend, seasonal, holiday).
     * Falls back to base price if no special rule found.
     */
    @Transactional(readOnly = true)
    public PricingResponse calculatePrice(UUID unitId, LocalDate checkIn, LocalDate checkOut) {
        logger.info("Calculating price for unit {} from {} to {}", unitId, checkIn, checkOut);

        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date");
        }

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found: " + unitId));

        Map<LocalDate, BigDecimal> dailyPrices = new LinkedHashMap<>();
        BigDecimal totalBasePrice = BigDecimal.ZERO;

        LocalDate current = checkIn;
        while (current.isBefore(checkOut)) {
            BigDecimal dayPrice = resolvePriceForDate(unitId, unit.getBasePrice(), current);
            dailyPrices.put(current, dayPrice);
            totalBasePrice = totalBasePrice.add(dayPrice);
            current = current.plusDays(1);
        }

        BigDecimal cleaningFee = unit.getCleaningFee();
        BigDecimal totalPrice = totalBasePrice.add(cleaningFee);
        int numberOfNights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        return PricingResponse.builder()
                .unitId(unit.getUnitId().toString())
                .unitName(unit.getUnitName())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .numberOfNights(numberOfNights)
                .basePricePerNight(unit.getBasePrice())
                .cleaningFee(cleaningFee)
                .totalBasePrice(totalBasePrice)
                .totalPrice(totalPrice)
                .dailyPrices(dailyPrices)
                .build();
    }

    /**
     * Resolve the effective price for a single date.
     * Priority: SEASONAL > HOLIDAY > WEEKEND > BASE
     */
    private BigDecimal resolvePriceForDate(UUID unitId, BigDecimal basePrice, LocalDate date) {

        // Check seasonal pricing first (highest priority)
        Optional<UnitPricing> seasonal = unitPricingRepository.findActiveRuleForDate(
                unitId, PricingType.SEASONAL, date);
        if (seasonal.isPresent()) return seasonal.get().getPrice();

        // Check holiday pricing
        Optional<UnitPricing> holiday = unitPricingRepository.findActiveRuleForDate(
                unitId, PricingType.HOLIDAY, date);
        if (holiday.isPresent()) return holiday.get().getPrice();

        // Check weekend pricing (Friday and Saturday)
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.FRIDAY
                || date.getDayOfWeek() == DayOfWeek.SATURDAY;
        if (isWeekend) {
            Optional<UnitPricing> weekend = unitPricingRepository.findActiveRuleForDate(
                    unitId, PricingType.WEEKEND, date);
            if (weekend.isPresent()) return weekend.get().getPrice();
        }

        return basePrice;
    }
}