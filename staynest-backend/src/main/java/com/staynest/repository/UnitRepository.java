package com.staynest.repository;

import com.staynest.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    //Find all units for the property
    List<Unit> findByProperty_PropertyId(UUID propertyId);

    //Find available unit of the property
    List<Unit> findByProperty_PropertyIdAndIsAvailable(UUID propertyId,Boolean isAvailable);

    //Find units by bedroom count
    List<Unit> findByBedrooms(Integer bedrooms);

    //Find units by max guests
    List<Unit> findByMaxGuestsGreaterThanEqual(Integer guests);

    //Find units within price range
    List<Unit> findByBasePriceBetween(BigDecimal minPrice,BigDecimal maxPrice);

    //Find available unit in a property within price range
    @Query("SELECT u FROM Unit u " +
            "WHERE u.property.propertyId = :propertyId "+
            "AND u.isAvailable = true " +
            "AND u.basePrice BETWEEN :minPrice AND :maxPrice")
    List<Unit> findAvailableUnitsByPropertyAndPriceRange(
            @Param("propertyId") UUID propertyId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    //Count total units for a property
    long countByProperty_PropertyId(UUID propertyId);

    //Count available units for a property
    long countByProperty_PropertyIdAndIsAvailable(UUID propertyId, Boolean isAvailable);

}
