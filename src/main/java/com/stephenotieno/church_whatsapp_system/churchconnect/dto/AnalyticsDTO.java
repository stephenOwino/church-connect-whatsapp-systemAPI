package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private Long id;
    private LocalDate date;
    private Integer totalMessages;
    private Integer inboundMessages;
    private Integer outboundMessages;
    private Integer commandsExecuted;
    private Integer newRegistrations;
    private Integer offeringsInitiated;
    private Integer prayerRequests;
    private Integer pastorReplies;
    private Integer failedMessages;
    private Integer activeConversations;
    private Integer uniqueUsers;
}
