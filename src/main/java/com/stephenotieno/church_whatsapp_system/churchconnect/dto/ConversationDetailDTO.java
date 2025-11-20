package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailDTO {
    private Long id;
    private MemberBasicDTO member;
    private String phoneNumber;
    private String status;
    private Integer messageCount;
    private Integer unreadCount;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private List<MessageDTO> messages;
}
