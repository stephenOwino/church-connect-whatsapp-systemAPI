package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long conversationId;
    private String messageSid;
    private String phoneNumber;
    private String direction;
    private String messageBody;
    private String messageType;
    private String mediaUrl;
    private String status;
    private Boolean isCommand;
    private String commandType;
    private Boolean processed;
    private Boolean needsPastorReply;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
