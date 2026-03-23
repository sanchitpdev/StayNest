package com.staynest.repository;

import com.staynest.entity.AvailabilityCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AvailabilityCalendarRepository extends JpaRepository<AvailabilityCalendar, UUID> {

    Optional<AvailabilityCalendar> findByUnit_UnitIdAndDate(UUID unitId, LocalDate date);

    @Query("""
            SELECT ac FROM AvailabilityCalendar ac
            WHERE ac.unit.unitId = :unitId
            AND ac.date BETWEEN :startDate AND :endDate
            ORDER BY ac.date ASC
            """)
    List<AvailabilityCalendar> findByUnitIdAndDateRange(
            @Param("unitId") UUID unitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT ac FROM AvailabilityCalendar ac
            WHERE ac.unit.unitId = :unitId
            AND ac.date BETWEEN :startDate AND :endDate
            AND ac.isAvailable = true
            ORDER BY ac.date ASC
            """)
    List<AvailabilityCalendar> findAvailableDates(
            @Param("unitId") UUID unitId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    boolean existsByUnit_UnitIdAndDate(UUID unitId, LocalDate date);
}