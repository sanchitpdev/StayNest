package com.staynest.entity;

import com.staynest.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE bookings SET deleted_at = NOW() WHERE booking_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Booking {
    //===========Primary Key==============
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id",updatable = false,nullable = false)
    private UUID bookingId;

    //===========Date Fields=============
    @Column(name = "check_in_date",nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date",nullable = false)
    private LocalDate checkOutDate;

    //=========Guest Info=============
    @Column(name = "num_guests",nullable = false)
    private Integer numGuests;

    @Column(name = "special_request",columnDefinition = "TEXT")
    private String specialRequest;

    //==========Pricing===============
    @Column(name = "total_price",nullable = false,precision = 10,scale = 2)
    private BigDecimal totalPrice;

    //==========Status=============
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status",nullable = false)
    @Builder.Default
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    //==============Audit Fields==============
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //===========Relationship===============
    //Many bookings for one unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id",nullable = false)
    private Unit unit;

    //Many Bookings by one user id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private User guest;

    //One Booking has Many payment
    @OneToMany(mappedBy = "booking",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    //one booking can have one reviews
    @OneToOne(mappedBy = "booking",cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private Review review;

    //==============Helper Method===========
    //Calculate the number of nights for this booking
    public long getNumberOfNights(){
        return ChronoUnit.DAYS.between(checkInDate,checkOutDate);
    }

    //Check booking can be canceled
    public boolean canBeCancelled(){
        return bookingStatus != BookingStatus.CANCELLED && bookingStatus != BookingStatus.COMPLETED;
    }

    //check if booking can be reviewed
    //only completed bookings can be reviewed
    public boolean canBeReviewed(){
        return bookingStatus == BookingStatus.COMPLETED
                && LocalDate.now().isAfter(ChronoLocalDate.from(checkOutDate));//need to review
    }

    //check booking dates are overlap with given table
    public  boolean overlaps(LocalDate start,LocalDate end){
        return !(checkOutDate.isBefore(start) || checkInDate.isAfter(end));
    }

    //Valid Booking Status
    @PrePersist
    @PreUpdate
    private void validDates(){
        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)){
            throw new IllegalArgumentException("Check-out must be after check-in date");
        }
        if (checkInDate.isBefore(LocalDate.now())){
            throw new IllegalArgumentException("Check-in date can not be in the past");
        }
    }

}
