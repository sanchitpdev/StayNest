package com.staynest.service;


import com.staynest.dto.request.UnitCreateRequest;
import com.staynest.dto.response.UnitCreateResponse;
import com.staynest.entity.Property;
import com.staynest.entity.Unit;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for unit management operations
 */
@Service
public class UnitService {

    private static final Logger logger = LoggerFactory.getLogger(UnitService.class);

    @Autowired
    private UnitRepository unitRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    /**
     * Create a new Unit for a property
     * Only property owner can add units
     *
     * @param request - Unit creation data
     * @Param userID - ID of user creating a unit
     * return UnitCreateResponse
     */
    @Transactional
    public UnitCreateResponse createUnit(UnitCreateRequest request, UUID userId){
        logger.info("Creating  new unit for property: {}",request.getPropertyId());

        //step 1: Find the property
        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property not found with ID: " + request.getPropertyId()));

        //step 2: Verify user is the property owner
        if (!property.getHost().getUserId().equals(userId)){
        logger.warn("User {} attempt to add unit to property {} owned by {}",
                userId,request.getPropertyId(),property.getHost().getUserId());
        throw new UnauthorizedException("You can only add units to your own properties");
        }

        //step 3: Create unit entity
        Unit unit = Unit.builder()
                .property(property)
                .unitName(request.getUnitName())
                .unitNumber(request.getUnitNumber())
                .bedrooms(request.getBedrooms())
                .bathrooms(request.getBathrooms())
                .maxGuests(request.getMaxGuests())
                .squareFeet(request.getSquareFeet())
                .basePrice(request.getBasePrice())
                .cleaningFee(request.getCleaningFee())
                .isAvailable(request.getIsAvailable())
                .build();

        //step 4: Save Unit
        Unit savedUnit = unitRepository.save(unit);
        logger.info("Unit created successfully with Id {}",savedUnit.getUnitId());

        //step 5: Build and return response
        return  buildUnitResponse(savedUnit);
    }

    /**
     * Get unit by ID
     *
     * @param unitId - unit ID
     *return UnitCreateResponse
     */
    @Transactional(readOnly = true)
    public  UnitCreateResponse getUnitById(UUID unitId){
        logger.info("Fetching unit with ID: {}",unitId);

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: "+ unitId));

        return buildUnitResponse(unit);
    }

    /**
     * Get all units for a property
     *
     * @param propertyId - propertyID
     *return  List of UnitCreateResponse
     */

    @Transactional(readOnly = true)
    public List<UnitCreateResponse> getUnitsByProperty(UUID propertyId){
        logger.info("Fetching units for property: {}",propertyId);

        //Verify property exists
        if (!propertyRepository.existsById(propertyId)){
            throw new ResourceNotFoundException("Property not found with ID: "+propertyId);
        }

        List<Unit> units = unitRepository.findByProperty_PropertyId(propertyId);
        return units.stream()
                .map(this::buildUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all available units by property
     *
     * @param propertyID - PropertyId
     * @return  List of UnitCreateResponse
     */

    @Transactional(readOnly = true)
    public List<UnitCreateResponse> getAllAvailableUnitsByProperty(UUID propertyID){
    logger.info("Fetching available units for property: {}" , propertyID);

    //verify property exists
        if(!propertyRepository.existsById(propertyID)){
            throw new ResourceNotFoundException("Property not found with ID: " + propertyID);
        }

        List<Unit> units = unitRepository.findByProperty_PropertyIdAndIsAvailable(propertyID,true);
        return units.stream()
                .map(this::buildUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update unit.
     * Only the property owner can update units
     *
     * @param unitId - UnitID
     * @Param request - Update unit data
     * @Param userId - ID of user requesting update
     * @return UnitCreateResponse
     */

    @Transactional
    public UnitCreateResponse updateUnit(UUID unitId, UnitCreateRequest request, UUID userId){
        logger.info("Updating unit {} by user {}",unitId,userId);

        //step 1:Find unit
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(()-> new ResourceNotFoundException("Unit not found with ID:" + unitId));

        //step 2: Check authorization(only property owner can update)
        if (!unit.getProperty().getHost().getUserId().equals(userId)){
            logger.warn("User {} attempted to update unit {} in property owned by {}",
                    userId,unitId,unit.getProperty().getHost().getUserId());
            throw new UnauthorizedException("You can only update units in your own properties");
        }

        //Step 3: update fields
        unit.setUnitName(request.getUnitName());
        unit.setUnitNumber(request.getUnitNumber());
        unit.setBedrooms(request.getBedrooms());
        unit.setBathrooms(request.getBathrooms());
        unit.setMaxGuests(request.getMaxGuests());
        unit.setSquareFeet(request.getSquareFeet());
        unit.setBasePrice(request.getBasePrice());
        unit.setCleaningFee(request.getCleaningFee());
        unit.setIsAvailable(request.getIsAvailable());

        //step 4: Save update unit
        Unit updateUnit = unitRepository.save(unit);
        logger.info("Unit {} updated successfully ",unitId);

        //step 5: Build and return response
        return buildUnitResponse(updateUnit);
    }

    /**
     * Delete unit.
     * Only the property owner can delete units
     *
     * @Param  unitId - Unit ID
     * @Param userId - ID of user requesting deletion
     */

    @Transactional
    public void deleteUnit(UUID unitId,UUID userId){
        logger.info("Deleting unit {} by user {}",unitId,userId);

        //step 1: Find Unit
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResourceNotFoundException("Unit not found with ID: " + unitId));

        //step 2: Check authorization ( only property owner can delete)
        if (!unit.getProperty().getHost().getUserId().equals(userId)){
            logger.warn("User {} attempted to delete unit {} in property owned by {}",
                    userId,unitId,unit.getProperty().getHost().getUserId());
            throw new UnauthorizedException("You can only delete units in your own properties ");
        }

        //step 4: Delete unit
        unitRepository.delete(unit);
        logger.info("Unit {} deleted successfully",unitId);
    }

    /**
     * Helper method to build UnitCreateResponse from unit entity
     */

    private UnitCreateResponse buildUnitResponse(Unit unit){
        return UnitCreateResponse.builder()
                .unitId(unit.getUnitId().toString())
                .propertyId(unit.getProperty().getPropertyId().toString())
                .unitName(unit.getUnitName())
                .unitNumber(unit.getUnitNumber())
                .bedrooms(unit.getBedrooms())
                .bathrooms(unit.getBathrooms())
                .maxGuests(unit.getMaxGuests())
                .squareFeet(unit.getSquareFeet())
                .basePrice(unit.getBasePrice())
                .cleaningFee(unit.getCleaningFee())
                .totalPrice(unit.getTotalPrice())
                .isAvailable(unit.getIsAvailable())
                .createdAt(unit.getCreatedAt())
                .updatedAt(unit.getUpdatedAt())
                .build();
    }
}
