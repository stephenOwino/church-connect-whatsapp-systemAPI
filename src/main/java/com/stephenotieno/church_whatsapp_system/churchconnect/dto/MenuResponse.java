package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private Boolean success;
    private String messageSid;
    private String phoneNumber;
    private String message;
}