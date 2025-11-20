package com.stephenotieno.church_whatsapp_system.churchconnect.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pastor_queue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastorQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Message message;

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

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "priority")
    private String priority; // HIGH, MEDIUM, LOW

    @Column(name = "category")
    private String category; // PRAYER, COUNSELING, INQUIRY, COMPLAINT, OTHER

    @Column(name = "status")
    private String status; // PENDING, ASSIGNED, REPLIED, CLOSED

    @Column(name = "assigned_to")
    private Long assignedTo; // Admin/Pastor ID

    @Column(name = "pastor_reply", columnDefinition = "TEXT")
    private String pastorReply;

    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    @Column(name = "replied_by")
    private Long repliedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
        if (priority == null) {
            priority = "MEDIUM";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
