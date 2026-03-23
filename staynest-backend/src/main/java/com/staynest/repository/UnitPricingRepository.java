package com.staynest.repository;

import com.staynest.entity.UnitPricing;
import com.staynest.enums.PricingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitPricingRepository extends JpaRepository<UnitPricing, UUID> {

    List<UnitPricing> findByUnit_UnitId(UUID unitId);

    @Query("""
            SELECT up FROM UnitPricing up
            WHERE up.unit.unitId = :unitId
            AND up.pricingType = :type
            AND (up.startDate IS NULL OR up.startDate <= :date)
            AND (up.endDate IS NULL OR up.endDate >= :date)
            """)
    Optional<UnitPricing> findActiveRuleForDate(
            @Param("unitId") UUID unitId,
            @Param("type") PricingType type,
            @Param("date") LocalDate date
    );
}