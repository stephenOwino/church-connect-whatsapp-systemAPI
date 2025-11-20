package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private Long id;
    private String phoneNumber;
    private String memberName;
    private String status;
    private Integer messageCount;
    private Integer unreadCount;
    private LocalDateTime lastMessageAt;
}