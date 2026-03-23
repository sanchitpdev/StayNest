package com.staynest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "availability_calendar", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"unit_id", "date"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "calendar_id", updatable = false, nullable = false)
    private UUID calendarId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    // Effective price for this specific date (pre-computed)
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "booking_id")
    private UUID bookingId;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    // Helper methods
    public void markAsBooked(UUID bookingId) {
        this.isAvailable = false;
        this.bookingId = bookingId;
    }

    public void markAsAvailable() {
        this.isAvailable = true;
        this.bookingId = null;
    }
}