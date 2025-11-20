package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class AuthResponse {
    private Long churchId;
    private Long adminId;
    private String token;
    private String refreshToken;
    private String adminName;
    private String churchName;
}