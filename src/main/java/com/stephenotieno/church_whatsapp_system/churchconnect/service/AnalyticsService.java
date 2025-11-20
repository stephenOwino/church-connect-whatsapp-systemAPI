package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MessageAnalyticsRepository analyticsRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final ConversationRepository conversationRepository;
    private final CommandRepository commandRepository;
    private final PastorQueueRepository pastorQueueRepository;
    private final ChurchRepository churchRepository;

    @Transactional
    public void generateDailyAnalytics(Long churchId, LocalDate date) {
        log.info("📊 Generating analytics for church {} on {}", churchId, date);

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // Get all messages for the day
        List<Message> messages = messageRepository.findByChurchIdAndDateRange(
                churchId, startOfDay, endOfDay);

        // Calculate metrics
        int totalMessages = messages.size();
        int inboundMessages = (int) messages.stream()
                .filter(m -> "INBOUND".equals(m.getDirection()))
                .count();
        int outboundMessages = (int) messages.stream()
                .filter(m -> "OUTBOUND".equals(m.getDirection()))
                .count();
        int failedMessages = (int) messages.stream()
                .filter(m -> "FAILED".equals(m.getStatus()))
                .count();

        // Get commands
        List<Command> commands = commandRepository.findByChurchIdAndDateRange(
                churchId, startOfDay, endOfDay);
        int commandsExecuted = commands.size();
        int newRegistrations = (int) commands.stream()
                .filter(c -> "REGISTER".equals(c.getCommandType()))
                .count();
        int offeringsInitiated = (int) commands.stream()
                .filter(c -> "GIVE".equals(c.getCommandType()))
                .count();
        int prayerRequests = (int) commands.stream()
                .filter(c -> "PRAYER".equals(c.getCommandType()))
                .count();

        // Get pastor replies
        int pastorReplies = (int) messages.stream()
                .filter(m -> "OUTBOUND".equals(m.getDirection()) &&
                        m.getMessageBody() != null &&
                        m.getMessageBody().length() > 100)
                .count();

        // Get active conversations
        Long activeConversations = conversationRepository.countActiveConversations(churchId);

        // Get unique users
        Set<String> uniquePhones = messages.stream()
                .map(Message::getPhoneNumber)
                .collect(Collectors.toSet());
        int uniqueUsers = uniquePhones.size();

        // Create or update analytics
        MessageAnalytics analytics = analyticsRepository
                .findByChurchIdAndDate(churchId, date)
                .orElse(MessageAnalytics.builder()
                        .church(church)
                        .date(date)
                        .build());

        analytics.setTotalMessages(totalMessages);
        analytics.setInboundMessages(inboundMessages);
        analytics.setOutboundMessages(outboundMessages);
        analytics.setCommandsExecuted(commandsExecuted);
        analytics.setNewRegistrations(newRegistrations);
        analytics.setOfferingsInitiated(offeringsInitiated);
        analytics.setPrayerRequests(prayerRequests);
        analytics.setPastorReplies(pastorReplies);
        analytics.setFailedMessages(failedMessages);
        analytics.setActiveConversations(activeConversations.intValue());
        analytics.setUniqueUsers(uniqueUsers);

        analyticsRepository.save(analytics);

        log.info("✅ Analytics generated: {} messages, {} unique users", totalMessages, uniqueUsers);
    }

    @Transactional
    public void generateTodayAnalytics(Long churchId) {
        generateDailyAnalytics(churchId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDTO> getAnalytics(Long churchId, LocalDate startDate, LocalDate endDate) {
        return analyticsRepository.findByChurchIdAndDateBetweenOrderByDateAsc(
                        churchId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDTO> getRecentAnalytics(Long churchId, int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        return getAnalytics(churchId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public AnalyticsDTO getAnalyticsForDate(Long churchId, LocalDate date) {
        return analyticsRepository.findByChurchIdAndDate(churchId, date)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getSummaryStats(Long churchId, LocalDate startDate, LocalDate endDate) {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalMessages", analyticsRepository.sumTotalMessages(churchId, startDate, endDate));
        stats.put("newRegistrations", analyticsRepository.sumNewRegistrations(churchId, startDate, endDate));
        stats.put("offeringsInitiated", analyticsRepository.sumOfferingsInitiated(churchId, startDate, endDate));

        return stats;
    }

    private AnalyticsDTO mapToDTO(MessageAnalytics analytics) {
        return AnalyticsDTO.builder()
                .id(analytics.getId())
                .date(analytics.getDate())
                .totalMessages(analytics.getTotalMessages())
                .inboundMessages(analytics.getInboundMessages())
                .outboundMessages(analytics.getOutboundMessages())
                .commandsExecuted(analytics.getCommandsExecuted())
                .newRegistrations(analytics.getNewRegistrations())
                .offeringsInitiated(analytics.getOfferingsInitiated())
                .prayerRequests(analytics.getPrayerRequests())
                .pastorReplies(analytics.getPastorReplies())
                .failedMessages(analytics.getFailedMessages())
                .activeConversations(analytics.getActiveConversations())
                .uniqueUsers(analytics.getUniqueUsers())
                .build();
    }
}