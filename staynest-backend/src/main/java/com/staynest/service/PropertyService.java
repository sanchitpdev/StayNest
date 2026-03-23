package com.staynest.service;

import com.staynest.dto.request.PropertyCreateRequest;
import com.staynest.dto.request.PropertySearchRequest;
import com.staynest.dto.response.PagedResponse;
import com.staynest.dto.response.PropertyCreateResponse;
import com.staynest.dto.response.PropertyResponse;
import com.staynest.dto.response.UnitCreateResponse;
import com.staynest.entity.Property;
import com.staynest.entity.Unit;
import com.staynest.entity.User;
import com.staynest.enums.PropertyType;
import com.staynest.enums.UserRole;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.ReviewRepository;
import com.staynest.repository.UnitRepository;
import com.staynest.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for property management operations
 * Handles CURD operations for properties
 */
@Service
public class PropertyService {
    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Create a new Property
     * Only Host can create a new property
     *
     * @Param request - Property Creation data
     * @Param userId - ID of the user who creating this property
     * @return PropertyCreateResponse
     */

    @Transactional
    public PropertyCreateResponse createProperty(PropertyCreateRequest request, UUID userId){
        logger.info("Creating new Property for user: {}",userId);

        //step1: Find the host user
        User host = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not found with ID: "+userId));

        //step 2: Verify user is a host
        if (host.getRole() != UserRole.HOST){
            logger.warn("User {} attempted to create property but is not a HOST",userId);
            throw new UnauthorizedException("Only users with the HOST role can create properties");
        }

        //step 3: Create a property entity
        Property property = Property.builder()
                .propertyName(request.getPropertyName())
                .description(request.getDescription())
                .propertyType(request.getPropertyType())
                .streetAddress(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .amenities(request.getAmenities())
                .host(host)
                .build();

        //Step 4: Save Property
        Property savedProperty = propertyRepository.save(property);
        logger.info("Property created successfully with Id: {}",savedProperty.getPropertyId());

        //step 5: Build and return response
        return PropertyCreateResponse.builder()
                .propertyId(savedProperty.getPropertyId().toString())
                .propertyName(savedProperty.getPropertyName())
                .description(savedProperty.getDescription())
                .propertyType(savedProperty.getPropertyType())
                .address(savedProperty.getStreetAddress())
                .city(savedProperty.getCity())
                .state(savedProperty.getState())
                .country(savedProperty.getCountry())
                .postalCode(savedProperty.getPostalCode())
                .latitude(savedProperty.getLatitude())
                .longitude(savedProperty.getLongitude())
                .amenities(savedProperty.getAmenities())
                .hostId(savedProperty.getHost().getUserId().toString())
                .createdAt(savedProperty.getCreatedAt())
                .build();
    }

    /**
     * Get Property by ID with full details
     *
     * @Param propertyId - propertyId
     * @return propertyResponse
     */

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(UUID propertyId){
        logger.info("Fetching property with ID: {}",propertyId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: "+ propertyId));

        return buildPropertyResponse(property);
    }

    /**
     * Get All properties
     * @return List of property response
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties(){
        logger.info("Fetching all properties");
        List<Property> properties = propertyRepository.findAll();
        return properties.stream()
                .map(this::buildPropertyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all property by hostId
     * @param hostId - Host user ID
     * @return List of PropertyResponse
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> getPropertiesByHost(UUID hostId){
        logger.info("Fetching properties for host: {}",hostId);

        List<Property> properties = propertyRepository.findByHost_UserId(hostId);
        return properties.stream()
                .map(this::buildPropertyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search property by city
     * @param city - City name
     * @return List of propertyResponse
     */
    @Transactional(readOnly = true)
    public List<PropertyResponse> searchPropertiesByCity(String city){
        logger.info("Searching properties in city: {}",city);

        List<Property> properties = propertyRepository.findByCity(city);

        return properties.stream()
                .map(this::buildPropertyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search Property by property type
     *
     * @param type - Property type
     * @return List of PropertyResponse
     */

    @Transactional(readOnly = true)
    public List<PropertyResponse> searchPropertiesByType(PropertyType type){
        logger.info("Searching properties of type: {}",type);

        List<Property> properties = propertyRepository.findByPropertyType(type);

        return properties.stream()
                .map(this::buildPropertyResponse)
                .collect(Collectors.toList());
    }

    /** 
     * Update Property
     * Only the property owner can update
     *
     * @param propertyId - Property ID
     * @Param request - Updated property data
     * @Param userId - ID of User requesting update
     * @return propertyCreateResponse
     */
    @Transactional
    public PropertyCreateResponse updateProperty(UUID propertyId, @Valid PropertyCreateRequest request, UUID userId){
        logger.info("Updating property {} by user {}",propertyId,userId);

        //step 1: Find Property
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(()-> new ResourceNotFoundException("Property not found with ID: "+propertyId));

        //step 2: Check authorization (only owner can update)
        if (!property.getHost().getUserId().equals(userId)){
            logger.warn("User {} attempted to update property {} owned by {} ",
                    userId, propertyId, property.getHost().getUserId());
            throw new UnauthorizedException("You can only update your own properties");
        }

        //step 3: Update fields
        property.setPropertyName(request.getPropertyName());
        property.setDescription(request.getDescription());
        property.setPropertyType(request.getPropertyType());
        property.setStreetAddress(request.getAddress());
        property.setCity(request.getCity());
        property.setState(request.getState());
        property.setCountry(request.getCountry());
        property.setPostalCode(request.getPostalCode());
        property.setLatitude(request.getLatitude());
        property.setLongitude(request.getLongitude());
        property.setAmenities(request.getAmenities());

        //step 4: Saved updated filed
        Property updatedProperty = propertyRepository.save(property);
        logger.info("Property {} updated successfully",propertyId);

        //step 5: Build and return response
        return PropertyCreateResponse.builder()
                .propertyId(updatedProperty.getPropertyId().toString())
                .propertyName(updatedProperty.getPropertyName())
                .description(updatedProperty.getDescription())
                .propertyType(updatedProperty.getPropertyType())
                .address(updatedProperty.getStreetAddress())
                .city(updatedProperty.getCity())
                .state(updatedProperty.getState())
                .country(updatedProperty.getCountry())
                .postalCode(updatedProperty.getPostalCode())
                .latitude(updatedProperty.getLatitude())
                .longitude(updatedProperty.getLongitude())
                .amenities(updatedProperty.getAmenities())
                .hostId(updatedProperty.getHost().getUserId().toString())
                .createdAt(updatedProperty.getCreatedAt())
                .build();
    }

    /**
     * Delete property
     * Only the property owner can delete
     * @Param  propertyId - Property ID
     * @param  userId - ID of user requesting to delete
     */

    @Transactional
    public void deleteProperty(UUID propertyId, UUID userId){
        logger.info("Deleting property {} by user {}",propertyId,userId);

        //step 1: Find property
       Property property = propertyRepository.findById(propertyId)
               .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: "+ propertyId));

        //step 2: Check authorization (only owner can delete)
        if (!property.getHost().getUserId().equals(userId)){
            logger.warn("User {} attempt to  delete property {} owned by {}",
                    userId,propertyId,property.getHost().getUserId());
            throw new UnauthorizedException("You can delete your own property");
        }

        //step 3: Delete property(cascade will delete units)
        propertyRepository.delete(property);
        logger.info("Property {} deleted successfully ",propertyId);
    }

    /**
     * Get all properties with pagination
     * @param page - Page Number (0-based)
     * @param size - Page Size
     * @param sortBy - Sort field(e.g, "PropertyName","CreatedAt")
     * @param sortDirection - Sort Direction ("asc" or "desc")
     * @return PagedResponse of PropertyResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<PropertyResponse> getAllPropertiesPaginated(
            int page, int size,String sortBy, String sortDirection){
        logger.info("Fetching properties - page: {},size: {}, sortBy: {}",page,size,sortBy);

        //Create sort
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        //Create pageable
        Pageable pageable = PageRequest.of(page,size,sort);

        //Fetch properties
        Page<Property> propertyPage = propertyRepository.findAll(pageable);

        //Convert to PropertyResponse
        Page<PropertyResponse> responsePage = propertyPage.map(this::buildPropertyResponse);

        return PagedResponse.of(responsePage);
    }

    /**
     * Advanced property search with multiple filters
     * @param searchRequest - search criteria
     * @return PagedResponse of PropertyResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<PropertyResponse> advancedSearch(PropertySearchRequest searchRequest){
        logger.info("Advanced property search: {}",searchRequest);

        //Create Sort
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "createdAt";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "desc" ;

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        //Create pageable
        Pageable pageable = PageRequest.of(
                searchRequest.getPageNumber(),
                searchRequest.getPageSize(),
                sort
        );

        //Execute search
        Page<Property> propertyPage = propertyRepository.searchProperties(
                searchRequest.getCity(),
                searchRequest.getState(),
                searchRequest.getCountry(),
                searchRequest.getPropertyType(),
                searchRequest.getMinBedrooms(),
                searchRequest.getMaxBedrooms(),
                searchRequest.getMinGuests(),
                pageable
        );

        //Filter by price range if specified
        if (searchRequest.getMinPrice() != null || searchRequest.getMaxPrice() != null){
            propertyPage = propertyRepository.searchByPriceRange(
                    searchRequest.getMinPrice(),
                    searchRequest.getMaxPrice(),
                    pageable
            );
        }
        //Convert to propertyResponse
        Page<PropertyResponse> responsePage = propertyPage.map(this::buildPropertyResponse);

        //Filter by rating if specified (post-query filter)
        if (searchRequest.getMinRating() != null){
            List<PropertyResponse> filtered = responsePage.getContent().stream()
                    .filter(p -> {
                        Double rating = reviewRepository.calculateAverageRating(
                                UUID.fromString(p.getPropertyId())
                        );
                        return rating != null && rating >= searchRequest.getMinRating();
                    })
                    .collect(java.util.stream.Collectors.toList());

            return PagedResponse.<PropertyResponse>builder()
                    .content(filtered)
                    .page(responsePage.getNumber())
                    .size(responsePage.getSize())
                    .totalElements(filtered.size())
                    .totalPage((int)Math.ceil(filtered.size()/(double)responsePage.getSize()))
                    .first(responsePage.isFirst())
                    .last(responsePage.isLast())
                    .empty(filtered.isEmpty())
                    .build();
        }
        return PagedResponse.of(responsePage);
    }

    /**
     * Search properties by city with pagination
     * @param city - City name
     * @param page - Page Number
     * @param size - Page Size
     * @return PagedResponse of PropertyResponse
     */
    @Transactional(readOnly = true)
    public PagedResponse<PropertyResponse> searchPropertiesByCityPaginated(
            String city, int page,int size){
        logger.info("Searching properties in city: {} - page: {}, size: {}",city,page,size);

        Pageable pageable = PageRequest.of(page,size,Sort.by("createdAt").descending());
        Page<Property> propertyPage = propertyRepository.findByCity(city,pageable);
        Page<PropertyResponse> responsePage = propertyPage.map(this::buildPropertyResponse);

        return PagedResponse.of(responsePage);
    }

    //Helper Method to build PropertyResponse form Property Entity

    private PropertyResponse buildPropertyResponse(Property property) {
        //Get Units for this property
        List<Unit> units = property.getUnits();

        //Calculate statistics
        int totalUnits = units.size();
        int availableUnits = (int) units.stream().filter(Unit ::getIsAvailable).count();

        //Find the lowest unit price
        BigDecimal startingPrice = units.stream()
                .map(Unit :: getBasePrice)
                .min(BigDecimal :: compareTo)
                .orElse(BigDecimal.ZERO);

        List<UnitCreateResponse> unitResponses = units.stream()
                .map(this :: buildUnitResponse)
                .collect(Collectors.toList());

        return PropertyResponse.builder()
                .propertyId(property.getPropertyId().toString())
                .propertyName(property.getPropertyName())
                .description(property.getDescription())
                .propertyType(property.getPropertyType())
                .address(property.getStreetAddress())
                .city(property.getCity())
                .state(property.getState())
                .country(property.getCountry())
                .postalCode(property.getPostalCode())
                .latitude(property.getLatitude())
                .longitude(property.getLongitude())
                .amenities(property.getAmenities())
                .hostId(property.getHost().getUserId().toString())
                .hostName(property.getHost().getFullName())
                .hostEmail(property.getHost().getEmail())
                .totalUnits(totalUnits)
                .availableUnits(availableUnits)
                .startingPrice(startingPrice)
                .createdAt(property.getCreatedAt())
                .updatedAt(property.getUpdatedAt())
                .units(unitResponses)
                .build();
    }

    //Helper method to build UnitCreateResponse from Unit entity
    private UnitCreateResponse buildUnitResponse(Unit unit) {
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
