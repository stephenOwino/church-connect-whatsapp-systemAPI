package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommandStatsDTO {
    private Long totalCommands;
    private Long successfulCommands;
    private Long failedCommands;
    private Map<String, Long> commandTypeCounts;
    private Map<String, Double> averageExecutionTimes;
    private String mostUsedCommand;
}