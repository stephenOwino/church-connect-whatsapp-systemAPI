package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {

    // Member Stats
    private Long totalMembers;
    private Long activeMembers;
    private Long newMembersToday;
    private Long newMembersThisWeek;
    private Long newMembersThisMonth;

    // Message Stats
    private Long totalMessages;
    private Long messagesToday;
    private Long messagesThisWeek;
    private Long messagesThisMonth;
    private Long inboundMessages;
    private Long outboundMessages;

    // Conversation Stats
    private Long activeConversations;
    private Long unreadConversations;

    // Pastor Queue Stats
    private Long pendingPastorReplies;
    private Long repliedToday;
    private Long highPriorityQueue;

    // Command Stats
    private Long commandsExecuted;
    private Long commandsToday;
    private Map<String, Long> topCommands;

    // Offering Stats
    private BigDecimal totalOfferings;
    private BigDecimal offeringsThisMonth;
    private Long offeringsCount;
    private Long offeringsInitiatedToday;

    // Engagement Stats
    private Double averageResponseTime;
    private Double messageSuccessRate;
    private Integer uniqueActiveUsersToday;

    // Charts Data
    private List<DailyStatsDTO> dailyStats;
    private List<CommandUsageDTO> commandUsage;
}