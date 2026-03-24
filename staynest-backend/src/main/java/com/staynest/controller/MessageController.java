package com.staynest.controller;

import com.staynest.dto.request.MessageRequest;
import com.staynest.dto.response.ConversationResponse;
import com.staynest.dto.response.MessageResponse;
import com.staynest.entity.User;
import com.staynest.service.ConversationService;
import com.staynest.service.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationService conversationService;

    /**
     * Start or get existing conversation for a property
     * POST /api/v1/messages/conversations/property/{propertyId}
     */
    @PostMapping("/conversations/property/{propertyId}")
    public ResponseEntity<ConversationResponse> getOrCreateConversation(
            @PathVariable UUID propertyId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ConversationResponse response = conversationService.getOrCreateConversation(propertyId, user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all my conversations
     * GET /api/v1/messages/conversations
     */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationResponse>> getMyConversations(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<ConversationResponse> conversations = conversationService.getMyConversations(user.getUserId());
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a conversation with full message history
     * GET /api/v1/messages/conversations/{conversationId}
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable UUID conversationId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ConversationResponse response = conversationService.getConversationWithMessages(conversationId, user.getUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Send a message in a conversation
     * POST /api/v1/messages/conversations/{conversationId}/send
     */
    @PostMapping("/conversations/{conversationId}/send")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable UUID conversationId,
            @Valid @RequestBody MessageRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        MessageResponse response = messageService.sendMessage(conversationId, request, user.getUserId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Get all messages in a conversation
     * GET /api/v1/messages/conversations/{conversationId}/messages
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable UUID conversationId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<MessageResponse> messages = messageService.getMessages(conversationId, user.getUserId());
        return ResponseEntity.ok(messages);
    }
}