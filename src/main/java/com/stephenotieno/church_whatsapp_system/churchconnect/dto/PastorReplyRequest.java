package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastorReplyRequest {

    @NotNull(message = "Queue ID is required")
    private Long queueId;

    @NotBlank(message = "Reply message is required")
    @Size(max = 4096, message = "Reply too long (max 4096 characters)")
    private String replyMessage;

    private String notes;

    private String status; // REPLIED, CLOSED
}