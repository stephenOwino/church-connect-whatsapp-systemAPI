package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandUsageDTO {
    private String commandType;
    private Long count;
    private Double percentage;
}