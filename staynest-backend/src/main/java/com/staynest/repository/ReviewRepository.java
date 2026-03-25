package com.staynest.repository;

import com.staynest.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    //Find All reviews for a property
    List<Review> findByProperty_PropertyId(UUID propertyId);

    //Find reviews By reviewers
    List<Review> findByReviewer_UserId(UUID reviewerId);

    //find review for specific booking
    Optional<Review> findByBooking_BookingId(UUID bookingId);

    //Find reviews with minimum  rating
    List<Review> findByProperty_PropertyIdAndRatingGreaterThanEqual(
            UUID propertyId,
            Integer minRating
    );

    //Calculate a average rating for a property
    @Query("SELECT AVG(r.rating) FROM Review r "+
            "WHERE r.property.propertyId = :propertyId ")
    Double calculateAverageRating(@Param("propertyId") UUID propertyId);

    //Calculate average cleanliness rating
    @Query("SELECT AVG(r.cleanlinessRating) FROM Review r "+
            "WHERE r.property.propertyId = :propertyId " +
            "AND r.cleanlinessRating IS NOT NULL")
    Double calculateAverageCleanlinessRating(@Param("propertyId") UUID propertyId);

    //Count total review for a property
    long countByProperty_PropertyId(UUID propertyId);

    //find Latest review for property
    List<Review> findTop10ByProperty_PropertyIdOrderByCreatedAtDesc(UUID propertyId);

    @Query("SELECT r FROM Review r "+
            "WHERE r.property.host.userId = :hostId")
    List<Review> findReviewsForHostProperties(@Param("hostId") UUID hostId);

    Page<Review> findByProperty_PropertyId(UUID propertyId, Pageable pageable);


}
