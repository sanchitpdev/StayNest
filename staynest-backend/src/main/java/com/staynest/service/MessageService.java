package com.staynest.service;

import com.staynest.dto.request.MessageRequest;
import com.staynest.dto.response.MessageResponse;
import com.staynest.entity.Conversation;
import com.staynest.entity.Message;
import com.staynest.entity.User;
import com.staynest.enums.MessageSender;
import com.staynest.enums.UserRole;
import com.staynest.exception.ResourceNotFoundException;
import com.staynest.exception.UnauthorizedException;
import com.staynest.repository.ConversationRepository;
import com.staynest.repository.MessageRepository;
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
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConversationService conversationService;

    /**
     * Send a message in a conversation.
     * Automatically determines if sender is GUEST or HOST.
     */
    @Transactional
    public MessageResponse sendMessage(UUID conversationId, MessageRequest request, UUID senderId) {
        logger.info("Sending message in conversation {} by user {}", conversationId, senderId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + senderId));

        // Verify sender is part of this conversation
        boolean isGuest = conversation.getGuest().getUserId().equals(senderId);
        boolean isHost  = conversation.getHost().getUserId().equals(senderId);

        if (!isGuest && !isHost) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        MessageSender senderRole = isGuest ? MessageSender.GUEST : MessageSender.HOST;

        // Create and save message
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .sentBy(senderRole)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Update conversation read status and last message time
        if (isGuest) {
            conversation.markAsUnreadForHost();
        } else {
            conversation.markAsUnreadForGuest();
        }
        conversationRepository.save(conversation);

        logger.info("Message {} sent successfully", savedMessage.getMessageId());

        return MessageResponse.builder()
                .messageId(savedMessage.getMessageId().toString())
                .conversationId(conversation.getConversationId().toString())
                .senderId(sender.getUserId().toString())
                .senderName(sender.getFullName())
                .sentBy(senderRole)
                .content(savedMessage.getContent())
                .isRead(savedMessage.getIsRead())
                .readAt(savedMessage.getReadAt())
                .createdAt(savedMessage.getCreatedAt())
                .build();
    }

    /**
     * Get all messages in a conversation
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(UUID conversationId, UUID userId) {
        logger.info("Fetching messages for conversation {}", conversationId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found: " + conversationId));

        // Only participants can read messages
        if (!conversation.getGuest().getUserId().equals(userId) &&
                !conversation.getHost().getUserId().equals(userId)) {
            throw new UnauthorizedException("You are not part of this conversation");
        }

        return messageRepository
                .findByConversation_ConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(m -> MessageResponse.builder()
                        .messageId(m.getMessageId().toString())
                        .conversationId(conversationId.toString())
                        .senderId(m.getSender().getUserId().toString())
                        .senderName(m.getSender().getFullName())
                        .sentBy(m.getSentBy())
                        .content(m.getContent())
                        .isRead(m.getIsRead())
                        .readAt(m.getReadAt())
                        .createdAt(m.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}