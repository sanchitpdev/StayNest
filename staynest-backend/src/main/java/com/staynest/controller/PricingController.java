package com.staynest.controller;

import com.staynest.dto.response.PricingResponse;
import com.staynest.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @Autowired
    private PricingService pricingService;

    /**
     * Get price breakdown for a unit and date range
     * GET /api/v1/pricing/units/{unitId}?checkIn=2026-04-01&checkOut=2026-04-05
     */
    @GetMapping("/units/{unitId}")
    public ResponseEntity<PricingResponse> getPrice(
            @PathVariable UUID unitId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        PricingResponse response = pricingService.calculatePrice(unitId, checkIn, checkOut);
        return ResponseEntity.ok(response);
    }
}