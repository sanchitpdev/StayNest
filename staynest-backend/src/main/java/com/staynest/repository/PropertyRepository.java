package com.staynest.repository;

import com.staynest.entity.Property;
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

    //Find  all property by host
    List<Property> findByHost_UserId(UUID hostId);

    //Find property by city
    List<Property> findByCity(String city);

    //find property by city and country
    List<Property> findByCityAndCountry(String city,String country);

    //find property by type
    List<Property> findByPropertyType(PropertyType propertyType);

    //find property by city and type
    List<Property> findByCityAndPropertyType(String city,PropertyType propertyType);

    //Search property by name(partial name)
    List<Property> findByPropertyNameContainingIgnoreCase(String name);

    //Custom query : Find properties within a city at least one available unit
    @Query("SELECT DISTINCT p FROM Property p " +
            "JOIN p.units u " +
            "WHERE p.city = :city AND u.isAvailable = true")
    List<Property> findAvailablePropertiesByCity(@Param("city") String city);

    //Count properties by host
    long countByHost_UserId(UUID hostId);

    /**
     * Find all properties with pagination
     */
    Page<Property> findAll(Pageable pageable);

    /**
     * Find properties by city with pagination
     */
    Page<Property> findByCity(String city, Pageable pageable);

    /**
     * Find property by type with pagination
     */
    Page<Property> findByPropertyType(PropertyType propertyType, Pageable pageable);

    /**
     * Find properties by host with pagination
     */
    Page<Property> findByHost_UserId(UUID hostId, Pageable pageable);

    /**
     * Advanced search with multiple filters
     */
    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN p.units u " +
            "WHERE (:city IS NULL OR p.city = :city) " +
            "AND (:state IS NULL OR p.state = :state) " +
            "AND (:country IS NULL OR p.country = :country) " +
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

    /**
     * Search properties with price range filter
     */
    @Query("SELECT DISTINCT p FROM Property p " +
            "LEFT JOIN p.units u " +
            "WHERE (:minPrice IS NULL OR u.basePrice >= :minPrice) " +
            "AND (:maxPrice IS NULL OR u.basePrice <= :maxPrice)")
    Page<Property> searchByPriceRange(
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            Pageable pageable
    );





}
