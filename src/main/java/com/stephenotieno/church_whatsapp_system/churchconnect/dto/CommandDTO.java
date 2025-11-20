package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandDTO {
    private Long id;
    private Long memberId;
    private String memberName;
    private String phoneNumber;
    private String commandType;
    private String commandText;
    private String parameters;
    private Boolean success;
    private String errorMessage;
    private String responseSent;
    private Long executionTimeMs;
    private LocalDateTime createdAt;
}