package com.staynest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"guest_id", "host_id", "property_id"})
})
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE conversations SET deleted_at = NOW() WHERE conversation_id = ?")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversation_id", updatable = false, nullable = false)
    private UUID conversationId;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "is_read_by_guest", nullable = false)
    @Builder.Default
    private Boolean isReadByGuest = true;

    @Column(name = "is_read_by_host", nullable = false)
    @Builder.Default
    private Boolean isReadByHost = true;

    // Audit fields
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private User guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Message> messages = new ArrayList<>();

    // Helper methods
    public void markAsUnreadForHost() {
        this.isReadByHost = false;
        this.lastMessageAt = LocalDateTime.now();
    }

    public void markAsUnreadForGuest() {
        this.isReadByGuest = false;
        this.lastMessageAt = LocalDateTime.now();
    }

    public int getUnreadCount(boolean isGuest) {
        if (isGuest) return isReadByGuest ? 0 : 1;
        return isReadByHost ? 0 : 1;
    }
}