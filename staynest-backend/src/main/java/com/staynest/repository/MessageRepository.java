package com.staynest.repository;

import com.staynest.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    // Get all messages for a conversation ordered by time
    List<Message> findByConversation_ConversationIdOrderByCreatedAtAsc(UUID conversationId);

    // Mark all unread messages in a conversation as read
    @Modifying
    @Query("""
            UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP
            WHERE m.conversation.conversationId = :conversationId
            AND m.isRead = false
            AND m.sender.userId != :userId
            """)
    void markAllAsRead(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );

    // Count unread messages for a user in a conversation
    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.conversation.conversationId = :conversationId
            AND m.isRead = false
            AND m.sender.userId != :userId
            """)
    long countUnread(
            @Param("conversationId") UUID conversationId,
            @Param("userId") UUID userId
    );
}