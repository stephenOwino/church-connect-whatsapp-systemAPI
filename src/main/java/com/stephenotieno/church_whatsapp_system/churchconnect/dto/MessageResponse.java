package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String messageSid;
    private String phoneNumber;
    private String status;
    private String message;
    private Boolean success;
}