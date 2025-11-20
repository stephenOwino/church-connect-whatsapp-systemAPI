package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class AnnouncementResponse {
    private Long announcementId;
    private Integer sentCount;
    private String sentAt;
}