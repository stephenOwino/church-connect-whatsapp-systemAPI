package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberRequest {
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+254[0-9]{9}$", message = "Phone must be in format +254XXXXXXXXX")
    private String phoneNumber;

    @NotBlank(message = "Full name is required")
    private String fullName;
}