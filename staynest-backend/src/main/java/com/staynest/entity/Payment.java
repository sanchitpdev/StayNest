package com.staynest.entity;

import com.staynest.enums.PaymentMethod;
import com.staynest.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    //==========Primary Key==========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id",updatable = false,nullable = false)
    private UUID paymentId;

    //=============Payment Fields=======
    @Column(name = "amount",nullable = false,precision = 10,scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method",nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status",nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "transaction_id",length = 255)
    private String transactionId;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    //===========Audit Fields=============
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    //==========Relationship============
    //Many Payments for one booking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id",nullable = false)
    private Booking booking;

    //==========Helper Method============
    //Check if Payments is successful
    public boolean isSuccessful(){
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    //Check if payment can be refunded
    public boolean canBeRefunded(){
        return paymentStatus == PaymentStatus.COMPLETED;
    }

    //Make Payment as completed
    public void markAsCompleted(String transactionId){
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.transactionId = transactionId;
        this.paymentDate = LocalDateTime.now();
    }

    //Mark payment As failed
    public void markAsFailed(){
        this.paymentStatus = PaymentStatus.FAILED;
    }

}
