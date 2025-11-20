package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyStatsDTO {
    private LocalDate date;
    private Integer messages;
    private Integer newMembers;
    private Integer offerings;
    private Integer commands;
}