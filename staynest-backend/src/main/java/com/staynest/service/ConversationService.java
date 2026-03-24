package com.staynest.service;

import com.staynest.dto.response.ConversationResponse;
import com.staynest.dto.response.MessageResponse;
import com.staynest.entity.Conversation;
import com.staynest.entity.Property;
import com.staynest.entity.User;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.ConversationRepository;
import com.staynest.repository.MessageRepository;
import com.staynest.repository.PropertyRepository;
import com.staynest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ConversationService {

    private static final Logger logger = LoggerFactory.getLogger(ConversationService.class);

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get or create a conversation between guest and host for a property.
     * If one already exists, returns it. Otherwise creates a new one.
     */
    @Transactional
    public ConversationResponse getOrCreateConversation(UUID propertyId, UUID guestId) {
        logger.info("Getting or creating conversation for property {} by guest {}", propertyId, guestId);

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found: " + propertyId));

        User guest = userRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + guestId));

        UUID hostId = property.getHost().getUserId();

        // Check if conversation already exists
        return conversationRepository
                .findByGuestHostAndProperty(guestId, hostId, propertyId)
                .map(c -> buildConversationResponse(c, guestId))
                .orElseGet(() -> {
                    // Create new conversation
                    Conversation conversation = Conversation.builder()
                            .guest(guest)
                            .host(property.getHost())
                            .property(property)
                            .build();

                    Conversation saved = conversationRepository.save(conversation);
                    logger.info("New conversation created: {}", saved.getConversationId());
                    return buildConversationResponse(saved, guestId);
                });
    }

    /**
     * Get all conversations for the current user (as guest or host)
     */
    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(UUID userId) {
        logger.info("Fetching conversations for user {}", userId);

        return conversationRepository.findAllByUserId(userId)
                .stream()
                .map(c -> buildConversationResponse(c, userId))
                .collect(Collectors.toList());
    }

    /**
     * Get a single conversation with all messages
     */
    @Transactional
    public ConversationResponse getConversationWithMessages(UUID conversationId, UUID userId) {
        logger.info("Fetching conversation {} for user {}", conversationId, userId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        // Only guest or host of this conversation can view it
        if (!conversation.getGuest().getUserId().equals(userId) &&
                !conversation.getHost().getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        // Mark messages as read
        messageRepository.markAllAsRead(conversationId, userId);

        // Update read flags
        if (conversation.getGuest().getUserId().equals(userId)) {
            conversation.setIsReadByGuest(true);
        } else {
            conversation.setIsReadByHost(true);
        }
        conversationRepository.save(conversation);

        return buildConversationResponse(conversation, userId);
    }

    // Helper method
    public ConversationResponse buildConversationResponse(Conversation c, UUID viewerId) {
        long unreadCount = messageRepository.countUnread(c.getConversationId(), viewerId);

        List<MessageResponse> messages = c.getMessages().stream()
                .map(m -> MessageResponse.builder()
                        .messageId(m.getMessageId().toString())
                        .conversationId(c.getConversationId().toString())
                        .senderId(m.getSender().getUserId().toString())
                        .senderName(m.getSender().getFullName())
                        .sentBy(m.getSentBy())
                        .content(m.getContent())
                        .isRead(m.getIsRead())
                        .readAt(m.getReadAt())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return ConversationResponse.builder()
                .conversationId(c.getConversationId().toString())
                .guestId(c.getGuest().getUserId().toString())
                .guestName(c.getGuest().getFullName())
                .hostId(c.getHost().getUserId().toString())
                .hostName(c.getHost().getFullName())
                .propertyId(c.getProperty().getPropertyId().toString())
                .propertyName(c.getProperty().getPropertyName())
                .lastMessageAt(c.getLastMessageAt())
                .isReadByGuest(c.getIsReadByGuest())
                .isReadByHost(c.getIsReadByHost())
                .createdAt(c.getCreatedAt())
                .messages(messages)
                .unreadCount(unreadCount)
                .build();
    }
}