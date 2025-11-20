package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Church name is required")
    private String churchName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+254[0-9]{9}$", message = "Phone must be in format +254XXXXXXXXX")
    private String phone;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Invalid email format")
    private String adminEmail;

    @NotBlank(message = "Admin name is required")
    private String adminName;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}