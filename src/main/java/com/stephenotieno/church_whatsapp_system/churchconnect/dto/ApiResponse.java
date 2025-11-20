package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private Boolean success;
    private String message;
    private T data;
}