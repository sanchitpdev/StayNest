package com.staynest.repository;

import com.staynest.entity.Property;
import com.staynest.enums.PropertyStatus;
import com.staynest.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    // Find all properties by host
    List<Property> findByHost_UserId(UUID hostId);

    // Find property by city — CASE INSENSITIVE
    List<Property> findByCityIgnoreCase(String city);

    // Find property by city with pagination — CASE INSENSITIVE
    Page<Property> findByCityIgnoreCase(String city, Pageable pageable);

    // Find property by city and country — CASE INSENSITIVE
    List<Property> findByCityIgnoreCaseAndCountryIgnoreCase(String city, String country);

    // Find property by type
    List<Property> findByPropertyType(PropertyType propertyType);

    // Find property by type with pagination
    Page<Property> findByPropertyType(PropertyType propertyType, Pageable pageable);

    // Find property by city and type — CASE INSENSITIVE
    List<Property> findByCityIgnoreCaseAndPropertyType(String city, PropertyType propertyType);

    // Search property by name (partial match) — already case insensitive
    List<Property> findByPropertyNameContainingIgnoreCase(String name);

    // Available properties in a city — CASE INSENSITIVE using LOWER()
    @Query("SELECT DISTINCT p FROM Property p " +
            "JOIN p.units u " +
            "WHERE LOWER(p.city) = LOWER(:city) AND u.isAvailable = true")
    List<Property> findAvailablePropertiesByCity(@Param("city") String city);

    // Count properties by host
    long countByHost_UserId(UUID hostId);

    // Find all properties with pagination
    Page<Property> findAll(Pageable pageable);

    // Find properties by host with pagination
    Page<Property> findByHost_UserId(UUID hostId, Pageable pageable);

    // Advanced search with multiple filters — CASE INSENSITIVE using LOWER()
    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN p.units u " +
            "WHERE (:city IS NULL OR LOWER(p.city) = LOWER(:city)) " +
            "AND (:state IS NULL OR LOWER(p.state) = LOWER(:state)) " +
            "AND (:country IS NULL OR LOWER(p.country) = LOWER(:country)) " +
            "AND (:propertyType IS NULL OR p.propertyType = :propertyType) " +
            "AND (:minBedrooms IS NULL OR u.bedrooms >= :minBedrooms) " +
            "AND (:maxBedrooms IS NULL OR u.bedrooms <= :maxBedrooms) " +
            "AND (:minGuests IS NULL OR u.maxGuests >= :minGuests)")
    Page<Property> searchProperties(
            @Param("city") String city,
            @Param("state") String state,
            @Param("country") String country,
            @Param("propertyType") PropertyType propertyType,
            @Param("minBedrooms") Integer minBedrooms,
            @Param("maxBedrooms") Integer maxBedrooms,
            @Param("minGuests") Integer minGuests,
            Pageable pageable
    );

    // Search by price range
    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN p.units u " +
            "WHERE (:minPrice IS NULL OR u.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR u.basePrice <= :maxPrice)")
    Page<Property> searchByPriceRange(
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );

    // Status-based queries
    List<Property> findByPropertyStatus(PropertyStatus propertyStatus);

    Page<Property> findByPropertyStatus(PropertyStatus propertyStatus, Pageable pageable);

    // Status + city — CASE INSENSITIVE
    List<Property> findByCityIgnoreCaseAndPropertyStatus(String city, PropertyStatus propertyStatus);
}