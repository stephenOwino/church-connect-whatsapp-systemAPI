package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message too long")
    private String message;

    private Long targetGroupId;
}