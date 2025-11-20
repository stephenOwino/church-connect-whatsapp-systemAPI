package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String phoneNumber;
    private String fullName;
    private String status;
    private String joinedDate;
    private String lastActive;
}