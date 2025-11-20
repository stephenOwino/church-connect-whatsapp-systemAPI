package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private String phoneNumber;
    private String status;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer messageCount;
    private Integer unreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MessageDTO> recentMessages;
}
