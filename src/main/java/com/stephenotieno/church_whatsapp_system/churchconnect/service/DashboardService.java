package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final PastorQueueRepository pastorQueueRepository;
    private final CommandRepository commandRepository;
    private final OfferingRepository offeringRepository;
    private final MessageAnalyticsRepository analyticsRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(Long churchId) {
        log.info("📊 Generating dashboard stats for church {}", churchId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.minusDays(7);
        LocalDateTime startOfMonth = now.minusDays(30);

        // Member Stats
        Long totalMembers = memberRepository.countByChurchId(churchId);
        Long activeMembers = memberRepository.countByChurchIdAndStatus(churchId, "ACTIVE");

        List<com.stephenotieno.church_whatsapp_system.churchconnect.entity.Member> allMembers =
                memberRepository.findByChurchId(churchId, PageRequest.of(0, 10000)).getContent();

        Long newMembersToday = allMembers.stream()
                .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(startOfToday))
                .count();
        Long newMembersThisWeek = allMembers.stream()
                .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(startOfWeek))
                .count();
        Long newMembersThisMonth = allMembers.stream()
                .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(startOfMonth))
                .count();

        // Message Stats
        Long totalMessages = messageRepository.count();
        Long inboundMessages = messageRepository.countByChurchIdAndDirection(churchId, "INBOUND");
        Long outboundMessages = messageRepository.countByChurchIdAndDirection(churchId, "OUTBOUND");

        List<com.stephenotieno.church_whatsapp_system.churchconnect.entity.Message> recentMessages =
                messageRepository.findByChurchIdAndDateRange(churchId, startOfToday, now);

        Long messagesToday = (long) recentMessages.size();
        Long messagesThisWeek = (long) messageRepository.findByChurchIdAndDateRange(
                churchId, startOfWeek, now).size();
        Long messagesThisMonth = (long) messageRepository.findByChurchIdAndDateRange(
                churchId, startOfMonth, now).size();

        // Conversation Stats
        Long activeConversations = conversationRepository.countActiveConversations(churchId);
        Long unreadConversations = (long) conversationRepository.findUnreadConversations(churchId).size();

        // Pastor Queue Stats
        Long pendingPastorReplies = pastorQueueRepository.countPendingByChurchId(churchId);
        Long repliedToday = pastorQueueRepository.findByChurchIdAndStatus(churchId, "REPLIED").stream()
                .filter(q -> q.getRepliedAt() != null && q.getRepliedAt().isAfter(startOfToday))
                .count();
        Long highPriorityQueue = pastorQueueRepository
                .findByChurchIdAndStatus(churchId, "PENDING").stream()
                .filter(q -> "HIGH".equals(q.getPriority()))
                .count();

        // Command Stats
        Long commandsExecuted = messageRepository.countCommandsByChurchId(churchId);
        Long commandsToday = (long) commandRepository.findByChurchIdAndDateRange(
                churchId, startOfToday, now).size();

        Map<String, Long> topCommands = commandRepository.countByCommandType(churchId).stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));

        // Offering Stats
        List<com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering> offerings =
                offeringRepository.findAll().stream()
                        .filter(o -> o.getChurch() != null && o.getChurch().getId().equals(churchId))
                        .filter(o -> "COMPLETED".equals(o.getStatus()))
                        .collect(Collectors.toList());

        BigDecimal totalOfferings = offerings.stream()
                .map(com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal offeringsThisMonth = offerings.stream()
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(startOfMonth))
                .map(com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long offeringsCount = (long) offerings.size();
        Long offeringsInitiatedToday = commandRepository.findByChurchIdAndDateRange(
                        churchId, startOfToday, now).stream()
                .filter(c -> "GIVE".equals(c.getCommandType()))
                .count();

        // Engagement Stats
        Double averageResponseTime = calculateAverageResponseTime(churchId);
        Double messageSuccessRate = calculateMessageSuccessRate(churchId);
        Integer uniqueActiveUsersToday = recentMessages.stream()
                .map(com.stephenotieno.church_whatsapp_system.churchconnect.entity.Message::getPhoneNumber)
                .collect(Collectors.toSet())
                .size();

        // Charts Data
        List<DailyStatsDTO> dailyStats = getDailyStats(churchId, 7);
        List<CommandUsageDTO> commandUsage = getCommandUsage(churchId);

        return DashboardStatsDTO.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .newMembersToday(newMembersToday)
                .newMembersThisWeek(newMembersThisWeek)
                .newMembersThisMonth(newMembersThisMonth)
                .totalMessages(totalMessages)
                .messagesToday(messagesToday)
                .messagesThisWeek(messagesThisWeek)
                .messagesThisMonth(messagesThisMonth)
                .inboundMessages(inboundMessages)
                .outboundMessages(outboundMessages)
                .activeConversations(activeConversations)
                .unreadConversations(unreadConversations)
                .pendingPastorReplies(pendingPastorReplies)
                .repliedToday(repliedToday)
                .highPriorityQueue(highPriorityQueue)
                .commandsExecuted(commandsExecuted)
                .commandsToday(commandsToday)
                .topCommands(topCommands)
                .totalOfferings(totalOfferings)
                .offeringsThisMonth(offeringsThisMonth)
                .offeringsCount(offeringsCount)
                .offeringsInitiatedToday(offeringsInitiatedToday)
                .averageResponseTime(averageResponseTime)
                .messageSuccessRate(messageSuccessRate)
                .uniqueActiveUsersToday(uniqueActiveUsersToday)
                .dailyStats(dailyStats)
                .commandUsage(commandUsage)
                .build();
    }

    private List<DailyStatsDTO> getDailyStats(Long churchId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        return analyticsRepository.findByChurchIdAndDateBetweenOrderByDateAsc(
                        churchId, startDate, endDate)
                .stream()
                .map(a -> DailyStatsDTO.builder()
                        .date(a.getDate())
                        .messages(a.getTotalMessages())
                        .newMembers(a.getNewRegistrations())
                        .offerings(a.getOfferingsInitiated())
                        .commands(a.getCommandsExecuted())
                        .build())
                .collect(Collectors.toList());
    }

    private List<CommandUsageDTO> getCommandUsage(Long churchId) {
        List<Object[]> commandCounts = commandRepository.countByCommandType(churchId);
        Long total = commandCounts.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        return commandCounts.stream()
                .map(row -> {
                    String commandType = (String) row[0];
                    Long count = ((Number) row[1]).longValue();
                    Double percentage = total > 0 ? (count * 100.0 / total) : 0.0;

                    return CommandUsageDTO.builder()
                            .commandType(commandType)
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Double calculateAverageResponseTime(Long churchId) {
        // Simplified calculation - can be enhanced
        List<Object[]> stats = commandRepository.getCommandStats(
                churchId, LocalDateTime.now().minusDays(30));

        if (stats.isEmpty()) return 0.0;

        return stats.stream()
                .mapToDouble(row -> ((Number) row[2]).doubleValue())
                .average()
                .orElse(0.0);
    }

    private Double calculateMessageSuccessRate(Long churchId) {
        Long total = messageRepository.countByChurchIdAndDirection(churchId, "OUTBOUND");
        if (total == 0) return 100.0;

        Long failed = commandRepository.countFailedCommands(churchId);
        Long successful = total - failed;

        return (successful * 100.0 / total);
    }
}