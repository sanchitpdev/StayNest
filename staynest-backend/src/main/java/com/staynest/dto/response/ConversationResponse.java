package com.staynest.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationResponse {
    private String conversationId;
    private String guestId;
    private String guestName;
    private String hostId;
    private String hostName;
    private String propertyId;
    private String propertyName;
    private LocalDateTime lastMessageAt;
    private Boolean isReadByGuest;
    private Boolean isReadByHost;
    private LocalDateTime createdAt;
    private List<MessageResponse> messages;
    private long unreadCount;
}