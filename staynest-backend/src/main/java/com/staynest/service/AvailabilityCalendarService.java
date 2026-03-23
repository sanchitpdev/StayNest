package com.staynest.service;

import com.staynest.entity.AvailabilityCalendar;
import com.staynest.entity.Unit;
import com.staynest.repository.AvailabilityCalendarRepository;
import com.staynest.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AvailabilityCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(AvailabilityCalendarService.class);
    private static final int PRE_POPULATE_YEARS = 2;

    @Autowired
    private AvailabilityCalendarRepository calendarRepository;

    @Autowired
    private UnitRepository unitRepository;

    /**
     * Pre-populate calendar for a single unit — 2 years from today.
     * Skips dates that already exist (safe to re-run).
     */
    @Transactional
    public void populateCalendarForUnit(UUID unitId) {
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unit not found: " + unitId));

        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(PRE_POPULATE_YEARS);

        List<AvailabilityCalendar> entries = new ArrayList<>();
        LocalDate current = start;

        while (!current.isAfter(end)) {
            if (!calendarRepository.existsByUnit_UnitIdAndDate(unitId, current)) {
                entries.add(AvailabilityCalendar.builder()
                        .unit(unit)
                        .date(current)
                        .isAvailable(true)
                        .build());
            }
            current = current.plusDays(1);
        }

        calendarRepository.saveAll(entries);
        logger.info("Populated {} calendar entries for unit {}", entries.size(), unitId);
    }

    /**
     * Pre-populate calendar for ALL units in the database.
     */
    @Transactional
    public void populateCalendarForAllUnits() {
        List<Unit> units = unitRepository.findAll();
        logger.info("Starting calendar population for {} units", units.size());

        for (Unit unit : units) {
            populateCalendarForUnit(unit.getUnitId());
        }

        logger.info("Calendar population complete for all units");
    }

    /**
     * Check if a date range is fully available for a unit.
     */
    @Transactional(readOnly = true)
    public boolean isRangeAvailable(UUID unitId, LocalDate checkIn, LocalDate checkOut) {
        List<AvailabilityCalendar> entries = calendarRepository.findByUnitIdAndDateRange(
                unitId, checkIn, checkOut.minusDays(1)
        );

        if (entries.isEmpty()) return false;

        return entries.stream().allMatch(AvailabilityCalendar::getIsAvailable);
    }

    /**
     * Block dates when a booking is confirmed.
     */
    @Transactional
    public void blockDates(UUID unitId, LocalDate checkIn, LocalDate checkOut, UUID bookingId) {
        List<AvailabilityCalendar> entries = calendarRepository.findByUnitIdAndDateRange(
                unitId, checkIn, checkOut.minusDays(1)
        );

        entries.forEach(entry -> entry.markAsBooked(bookingId));
        calendarRepository.saveAll(entries);
        logger.info("Blocked {} dates for unit {} booking {}", entries.size(), unitId, bookingId);
    }

    /**
     * Release dates when a booking is cancelled.
     */
    @Transactional
    public void releaseDates(UUID unitId, LocalDate checkIn, LocalDate checkOut) {
        List<AvailabilityCalendar> entries = calendarRepository.findByUnitIdAndDateRange(
                unitId, checkIn, checkOut.minusDays(1)
        );

        entries.forEach(AvailabilityCalendar::markAsAvailable);
        calendarRepository.saveAll(entries);
        logger.info("Released {} dates for unit {}", entries.size(), unitId);
    }
}