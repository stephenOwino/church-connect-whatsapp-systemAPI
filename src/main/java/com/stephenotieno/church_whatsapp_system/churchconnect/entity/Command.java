package com.stephenotieno.church_whatsapp_system.churchconnect.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "commands")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Command {

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

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "command_type", nullable = false)
    private String commandType; // REGISTER, GIVE, BALANCE, PRAYER, INFO, HELP

    @Column(name = "command_text", columnDefinition = "TEXT")
    private String commandText;

    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters; // JSON string of parsed parameters

    @Column(name = "success")
    private Boolean success = true;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "response_sent", columnDefinition = "TEXT")
    private String responseSent;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}