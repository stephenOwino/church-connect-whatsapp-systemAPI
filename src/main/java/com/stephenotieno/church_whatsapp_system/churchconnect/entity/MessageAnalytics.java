package com.stephenotieno.church_whatsapp_system.churchconnect.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageAnalytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Church church;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_messages")
    private Integer totalMessages = 0;

    @Column(name = "inbound_messages")
    private Integer inboundMessages = 0;

    @Column(name = "outbound_messages")
    private Integer outboundMessages = 0;

    @Column(name = "commands_executed")
    private Integer commandsExecuted = 0;

    @Column(name = "new_registrations")
    private Integer newRegistrations = 0;

    @Column(name = "offerings_initiated")
    private Integer offeringsInitiated = 0;

    @Column(name = "prayer_requests")
    private Integer prayerRequests = 0;

    @Column(name = "pastor_replies")
    private Integer pastorReplies = 0;

    @Column(name = "failed_messages")
    private Integer failedMessages = 0;

    @Column(name = "active_conversations")
    private Integer activeConversations = 0;

    @Column(name = "unique_users")
    private Integer uniqueUsers = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
