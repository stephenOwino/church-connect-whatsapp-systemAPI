package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PastorQueueDTO {
    private Long id;
    private Long messageId;
    private Long memberId;
    private String memberName;
    private String phoneNumber;
    private String messageBody;
    private String priority;
    private String category;
    private String status;
    private Long assignedTo;
    private String assignedToName;
    private String pastorReply;
    private LocalDateTime repliedAt;
    private String repliedByName;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}