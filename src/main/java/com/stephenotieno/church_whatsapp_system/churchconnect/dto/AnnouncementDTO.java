package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnnouncementDTO {
    private Long id;
    private String title;
    private String message;
    private ChurchBasicDTO church;
    private LocalDateTime sentAt;
    private Integer sentCount;
    private LocalDateTime createdAt;
}