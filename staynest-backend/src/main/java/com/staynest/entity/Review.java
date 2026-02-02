package com.staynest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.security.auth.callback.LanguageCallback;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    //=========Primary Key===========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id",updatable = false,nullable = false)
    private UUID reviewId;

    //===========Rating Fields=======
    @Min(value = 1,message = "Rating must be at least 1")
    @Max(value = 5,message = "Rating must be at most 5")
    @Column(name = "rating",nullable = false)
    private Integer rating;

    @Column(name = "comment",columnDefinition = "TEXT")
    private String comment;

    //==========Category Ratings========
    @Min(1) @Max(5)
    @Column(name = "cleanliness_rating")
    private Integer cleanlinessRating;

    @Min(1) @Max(5)
    @Column(name = "accuracy_rating")
    private Integer accuracyRating;

    @Min(1) @Max(5)
    @Column(name = "communication_rating")
    private Integer communicationRating;

    @Min(1) @Max(5)
    @Column(name = "location_rating")
    private Integer locationRating;

    @Min(1) @Max(5)
    @Column(name = "value_rating")
    private Integer valueRating;

    //==========Audit fields=====
    @CreatedDate
    @Column(name = "created_at", nullable = false,updatable = false)
    private LocalDateTime createdAt;

    //===========RelationShip========;
    //one review for one booking
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id",nullable = false,unique = true)
    private Booking booking;

    //Many Reviews for one property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id",nullable = false)
    private Property property;

    //Many Reviews written by one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id",nullable = false)
    private User reviewer;

    //==========Helper Method========
    //Calculate the average of all category ratings
    public Double getAverageRating(){
        if (cleanlinessRating == null || accuracyRating == null ||
                communicationRating == null ||locationRating == null||
                valueRating == null){
            return rating.doubleValue();
        }
        double sum = cleanlinessRating + accuracyRating + communicationRating +
                     locationRating+ valueRating;
        return sum/5.0;
    }

    //Check if all category ratings are updated
    public boolean hasAllCategoryRatings(){
        return cleanlinessRating != null && accuracyRating != null &&
                communicationRating != null && locationRating != null &&
                valueRating != null;
    }


}
