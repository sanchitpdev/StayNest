package com.staynest.controller;

import com.staynest.dto.request.UnitCreateRequest;
import com.staynest.dto.response.UnitCreateResponse;
import com.staynest.entity.User;
import com.staynest.service.UnitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for unit management
 * Handles all unit-related operations
 */
@RestController
@RequestMapping("/units")
public class    UnitController {

    @Autowired
    private UnitService unitService;

    /**
     * Create a new unit for a property
     * Only property owner can add units

     * POST api/v1/units
     */
    @PostMapping
    public ResponseEntity<UnitCreateResponse> createUnit(
            @Valid @RequestBody UnitCreateRequest request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();
        UnitCreateResponse response = unitService.createUnit(request,user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get Unit by ID
     * Public endpoint

     * GET /api/v1/units/{unitId}
     */
    @GetMapping("{unitId}")
    public ResponseEntity<UnitCreateResponse> getUnitById(@PathVariable UUID unitId){
        UnitCreateResponse response = unitService.getUnitById(unitId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all unit of property
     * public endpoint
     *GET /api/v1/property/{propertyId}
     */
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<UnitCreateResponse>> getUnitByProperty(@PathVariable UUID propertyId){
        List<UnitCreateResponse> units = unitService.getUnitsByProperty(propertyId);
        return ResponseEntity.ok(units);
    }

    /**
     * Get all available units for property
     * public endpoint - useful for showing bookable units

     * GET api/v1/units/property/propertyId/available
     */
    @GetMapping("/property/{propertyId}/available")
    public ResponseEntity<List<UnitCreateResponse>> getAllAvailableUnitsByProperty(@PathVariable UUID propertyId){
        List<UnitCreateResponse> units = unitService.getAllAvailableUnitsByProperty(propertyId);
        return ResponseEntity.ok(units);
    }

    /**
     * Update Unit
     * Only property owner can update

     * PUT /api/v1/units/{unitId}
     */
    @PutMapping("/{unitId}")
    public ResponseEntity<UnitCreateResponse> updateUnit(
            @PathVariable UUID unitId,
            @Valid @RequestBody UnitCreateRequest request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();
        UnitCreateResponse response = unitService.updateUnit(unitId,request,user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete unit
     * Only property owner can delete

     * DELETE /api/units/{unitId}
     */
    @DeleteMapping("/{unitId}")
    public ResponseEntity<Void> deleteUnit(
            @PathVariable UUID unitId,
            Authentication authentication
    ){
        User user = (User) authentication.getPrincipal();
        unitService.deleteUnit(unitId,user.getUserId());
        return ResponseEntity.noContent().build();
    }
}
