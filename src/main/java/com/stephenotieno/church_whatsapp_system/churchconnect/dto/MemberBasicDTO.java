package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberBasicDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String status;
}
