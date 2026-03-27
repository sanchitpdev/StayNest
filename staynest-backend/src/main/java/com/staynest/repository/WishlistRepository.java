package com.staynest.repository;

import com.staynest.entity.User;
import com.staynest.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    //Find all wishlists for a user
    List<Wishlist> findByUser_UserId(UUID userId);

    //Check if user have already saved a property
    boolean existsByUser_UserIdAndProperty_PropertyId(
            UUID userId,
            UUID PropertyId
    );

    //Find specific wishlist entry
    Optional<Wishlist> findByUser_UserIdAndProperty_PropertyId(
            UUID userId,
            UUID PropertyId
    );

    //Count How many users saved a property(popularity matric)
    long countByProperty_PropertyId(UUID PropertyId);

    //Delete wishlist entry
    void deleteByUser_UserIdAndProperty_PropertyId(
            UUID userId,
            UUID propertyId
    );

    //Whitelist of user in paginated format
    Page<Wishlist> findByUser_UserId(UUID userId, Pageable pageable);
}
