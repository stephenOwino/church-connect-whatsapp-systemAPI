package com.stephenotieno.church_whatsapp_system.churchconnect.Scheduler;


import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Church;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.ChurchRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.AnalyticsService;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final AnalyticsService analyticsService;
    private final ConversationService conversationService;
    private final ChurchRepository churchRepository;

    /**
     * Generate analytics every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyAnalytics() {
        log.info("🕐 Starting scheduled analytics generation...");

        List<Church> churches = churchRepository.findAll();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        for (Church church : churches) {
            try {
                analyticsService.generateDailyAnalytics(church.getId(), yesterday);
                log.info("✅ Analytics generated for church: {}", church.getName());
            } catch (Exception e) {
                log.error("❌ Failed to generate analytics for church {}: {}",
                        church.getName(),
                        church.getName(), e.getMessage());
            }
        }

        log.info("✅ Daily analytics generation completed");
    }

    /**
     * Generate current day analytics every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void generateHourlyAnalytics() {
        log.info("🕐 Updating today's analytics...");

        List<Church> churches = churchRepository.findAll();

        for (Church church : churches) {
            try {
                analyticsService.generateTodayAnalytics(church.getId());
                log.info("✅ Today's analytics updated for church: {}", church.getName());
            } catch (Exception e) {
                log.error("❌ Failed to update analytics for church {}: {}",
                        church.getName(), e.getMessage());
            }
        }
    }

    /**
     * Archive inactive conversations every week
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    public void archiveInactiveConversations() {
        log.info("🕐 Archiving inactive conversations...");

        List<Church> churches = churchRepository.findAll();
        int daysInactive = 30; // Archive conversations inactive for 30 days

        for (Church church : churches) {
            try {
                conversationService.archiveInactiveConversations(church.getId(), daysInactive);
                log.info("✅ Inactive conversations archived for church: {}", church.getName());
            } catch (Exception e) {
                log.error("❌ Failed to archive conversations for church {}: {}",
                        church.getName(), e.getMessage());
            }
        }

        log.info("✅ Conversation archiving completed");
    }

    /**
     * Clean up old data every month (optional)
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void monthlyCleanup() {
        log.info("🕐 Starting monthly data cleanup...");

        // Add cleanup logic here if needed
        // For example: delete very old archived conversations, old analytics data, etc.

        log.info("✅ Monthly cleanup completed");
    }
}