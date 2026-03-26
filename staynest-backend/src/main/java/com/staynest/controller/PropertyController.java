package com.staynest.controller;

import com.staynest.dto.request.PropertyCreateRequest;
import com.staynest.dto.request.PropertySearchRequest;
import com.staynest.dto.response.PagedResponse;
import com.staynest.dto.response.PropertyCreateResponse;
import com.staynest.dto.response.PropertyResponse;
import com.staynest.entity.User;
import com.staynest.enums.PropertyType;
import com.staynest.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for property management
 * Handles all property-related operations
 */
@RestController
@RequestMapping("/properties")
public class PropertyController {

    @Autowired
    private PropertyService propertyService;

    /**
     * Create a new property
     * Only Host's can create property

     * POST /api/v1/properties
     */
    @PostMapping
    public ResponseEntity<PropertyCreateResponse> createProperty(
            @Valid @RequestBody PropertyCreateRequest request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();
        PropertyCreateResponse response = propertyService.createProperty(request,user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get property by ID
     * public endpoint - anyone can view property details

     * GET /api/v1/properties/{propertyId}
     */
    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable UUID propertyId){
        PropertyResponse response = propertyService.getPropertyById(propertyId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all properties
     * Public endpoint - anyone can view all properties.

     * GET /api/v1/properties
     */

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties(){
        List<PropertyResponse> properties = propertyService.getAllProperties();
        return ResponseEntity.ok(properties);
    }

    /**
     * Get properties by hostId
     * Anyone can view a specific host's properties

     * GET /api/v1/properties/host/{hostId}
     */

    @GetMapping("/host/{hostId}")
    public ResponseEntity<List<PropertyResponse>> getPropertiesByHost(@PathVariable UUID hostId){
        List<PropertyResponse> properties = propertyService.getPropertiesByHost(hostId);
        return ResponseEntity.ok(properties);
    }

    /**
     * Get my properties (authenticated host)
     * Returns properties owned by the currently logged-in user

     * GET /api/v1/properties/my-properties
     */

    @GetMapping("/my-properties")
    public ResponseEntity<List<PropertyResponse>> getMyProperties(Authentication authentication){
        User user = (User) authentication.getPrincipal();
        List<PropertyResponse> properties = propertyService.getPropertiesByHost(user.getUserId());
        return ResponseEntity.ok(properties);
    }

    /**
     * Search property by city
     * public endpoint

     * GET /api/v1/properties/serch?city=Mumbai
     */

    @GetMapping("/search")
    public ResponseEntity<List<PropertyResponse>> searchProperties(
            @RequestParam(required = false)String city,
            @RequestParam(required = false)PropertyType type){

        List<PropertyResponse> properties;

        if (city != null ){
            properties = propertyService.searchPropertiesByCity(city);
        } else if (type != null) {
            properties = propertyService.searchPropertiesByType(type);
        }else {
            properties = propertyService.getAllProperties();
        }

        return ResponseEntity.ok(properties);
    }

    /**
     * Update property
     * Only the property owner can update.

     * PUT /api/v1/properties/{propertyId}
     */

    @PutMapping("/{propertyId}")
    public ResponseEntity<PropertyCreateResponse> updateProperty(
            @PathVariable UUID propertyId,
            @Valid @RequestBody PropertyCreateRequest request,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();
        PropertyCreateResponse response = propertyService.updateProperty(propertyId,request,user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Delete property
     * Only the property owner can only  delete
     * DELETE /api/v1/properties/{propertyId}
     */

    @DeleteMapping("/{propertyId}")
    public ResponseEntity<Void> deleteProperty(
            @PathVariable UUID propertyId,
            Authentication authentication){
        User user = (User) authentication.getPrincipal();

        propertyService.deleteProperty(propertyId,user.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all properties with pagination
     */
    @GetMapping("/paginated")
    public ResponseEntity<PagedResponse<PropertyResponse>> getAllPropertiesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection){
        PagedResponse<PropertyResponse> response = propertyService.getAllPropertiesPaginated(
                page,size,sortBy,sortDirection
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Advanced property search with filters
     */
    @PostMapping("/search/advanced")
    public ResponseEntity<PagedResponse<PropertyResponse>> advancedSearch(
            @RequestBody PropertySearchRequest searchRequest){

        PagedResponse<PropertyResponse> response = propertyService.advancedSearch(searchRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Search properties by city with pagination
     */
    @GetMapping("/search/city")
    public ResponseEntity<PagedResponse<PropertyResponse>> searchByCityPaginated(
            @RequestParam String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        PagedResponse<PropertyResponse> response = propertyService.searchPropertiesByCityPaginated(
                city, page, size
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Activate a property (host publishes their listing)
     * PATCH /api/v1/properties/{propertyId}/activate
     */
    @PatchMapping("/{propertyId}/activate")
    public ResponseEntity<PropertyCreateResponse> activateProperty(
            @PathVariable UUID propertyId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PropertyCreateResponse response = propertyService.activateProperty(propertyId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Deactivate a property (host hides it temporarily)
     * PATCH /api/v1/properties/{propertyId}/deactivate
     */
    @PatchMapping("/{propertyId}/deactivate")
    public ResponseEntity<PropertyCreateResponse> deactivateProperty(
            @PathVariable UUID propertyId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PropertyCreateResponse response = propertyService.deactivateProperty(propertyId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Suspend a property — ADMIN only
     * PATCH /api/v1/properties/{propertyId}/suspend
     */
    // Remove this import entirely if only used for suspend
// import org.springframework.security.access.prepost.PreAuthorize;

    @PatchMapping("/{propertyId}/suspend")
    public ResponseEntity<PropertyCreateResponse> suspendProperty(
            @PathVariable UUID propertyId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        PropertyCreateResponse response = propertyService.suspendProperty(propertyId, user.getUserId());
        return ResponseEntity.ok(response);
    }
}
