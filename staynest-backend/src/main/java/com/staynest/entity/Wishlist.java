package com.staynest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wishlists", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id","property_id"})
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE wishlists SET deleted_at = NOW() WHERE wishlist_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Wishlist {

    //=========Primary Key===========
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "wishlist_id",nullable = false,updatable = false)
    private UUID wishlistId;

    //==========Audit fields==========
    @CreatedDate
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "collection_name", length = 100)
    @Builder.Default
    private String collectionName = "Saved";

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    //Many wishlists for one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false)
    private User user;

    //Many wishlist for one property
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;
}
