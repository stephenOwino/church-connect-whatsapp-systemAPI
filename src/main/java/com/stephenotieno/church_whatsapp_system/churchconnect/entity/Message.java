package com.stephenotieno.church_whatsapp_system.churchconnect.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference
    @ToString.Exclude
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Church church;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    @JsonBackReference
    @ToString.Exclude
    private Conversation conversation;

    @Column(name = "message_sid", unique = true)
    private String messageSid;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "direction", nullable = false)
    private String direction; // INBOUND, OUTBOUND

    @Column(name = "message_body", columnDefinition = "TEXT")
    private String messageBody;

    @Column(name = "message_type")
    private String messageType; // TEXT, IMAGE, DOCUMENT, AUDIO, VIDEO

    @Column(name = "media_url")
    private String mediaUrl;

    @Column(name = "status")
    private String status; // SENT, DELIVERED, READ, FAILED

    @Column(name = "is_command")
    private Boolean isCommand = false;

    @Column(name = "command_type")
    private String commandType; // REGISTER, GIVE, BALANCE, PRAYER, INFO, HELP

    @Column(name = "processed")
    private Boolean processed = false;

    @Column(name = "needs_pastor_reply")
    private Boolean needsPastorReply = false;

    @Column(name = "error_message")
    private String errorMessage;

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
