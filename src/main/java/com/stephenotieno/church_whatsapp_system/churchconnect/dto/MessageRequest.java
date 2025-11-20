package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+254[0-9]{9}$", message = "Phone must be in format +254XXXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "Message body is required")
    @Size(max = 4096, message = "Message too long (max 4096 characters)")
    private String messageBody;

    private String messageType; // TEXT, IMAGE, DOCUMENT

    private String mediaUrl;
}