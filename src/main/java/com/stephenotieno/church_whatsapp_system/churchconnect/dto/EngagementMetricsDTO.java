package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EngagementMetricsDTO {
    private Long totalMembers;
    private Long activeLastWeek;
    private Long activeLastMonth;
    private Double weeklyEngagementRate;
    private Double monthlyEngagementRate;
    private Double avgMessagesPerMember;
}
