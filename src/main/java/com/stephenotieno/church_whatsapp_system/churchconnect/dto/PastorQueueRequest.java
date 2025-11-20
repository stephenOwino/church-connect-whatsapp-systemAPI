package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastorQueueRequest {

    @NotNull(message = "Message ID is required")
    private Long messageId;

    private String priority; // HIGH, MEDIUM, LOW

    @NotBlank(message = "Category is required")
    private String category; // PRAYER, COUNSELING, INQUIRY, COMPLAINT, OTHER

    private Long assignedTo;

    private String notes;
}
