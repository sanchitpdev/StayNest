package com.staynest.repository;

import com.staynest.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Find existing conversation between guest, host and property
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.guest.userId = :guestId
            AND c.host.userId = :hostId
            AND c.property.propertyId = :propertyId
            """)
    Optional<Conversation> findByGuestHostAndProperty(
            @Param("guestId") UUID guestId,
            @Param("hostId") UUID hostId,
            @Param("propertyId") UUID propertyId
    );

    // Get all conversations for a user (as guest or host)
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.guest.userId = :userId OR c.host.userId = :userId
            ORDER BY c.lastMessageAt DESC
            """)
    List<Conversation> findAllByUserId(@Param("userId") UUID userId);

    // Get unread conversations for host
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.host.userId = :hostId
            AND c.isReadByHost = false
            """)
    List<Conversation> findUnreadByHost(@Param("hostId") UUID hostId);

    // Get unread conversations for guest
    @Query("""
            SELECT c FROM Conversation c
            WHERE c.guest.userId = :guestId
            AND c.isReadByGuest = false
            """)
    List<Conversation> findUnreadByGuest(@Param("guestId") UUID guestId);
}