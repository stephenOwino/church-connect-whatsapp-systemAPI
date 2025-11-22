package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 60, message = "Title must be less than 60 characters")
    private String title;

    @Size(max = 1024, message = "Description must be less than 1024 characters")
    private String description;

    @NotEmpty(message = "At least one option is required")
    @Size(max = 10, message = "Maximum 10 options allowed")
    private List<String> options;

    private String footer;

    private String menuType; // BUTTON, LIST, TEXT

    private String phoneNumber;
}