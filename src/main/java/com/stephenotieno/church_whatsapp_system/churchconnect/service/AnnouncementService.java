package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AnnouncementRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AnnouncementResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Announcement;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Church;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Group;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Member;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.AnnouncementRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.ChurchRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.GroupRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ChurchRepository churchRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final WhatsAppMetaService whatsAppService;

    @Transactional
    public AnnouncementResponse sendAnnouncement(Long churchId, AnnouncementRequest request) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Group targetGroup = null;
        if (request.getTargetGroupId() != null) {
            targetGroup = groupRepository.findById(request.getTargetGroupId())
                    .orElseThrow(() -> new RuntimeException("Group not found"));
        }

        List<Member> recipients = getRecipients(churchId, targetGroup);

        Announcement announcement = Announcement.builder()
                .church(church)
                .title(request.getTitle())
                .message(request.getMessage())
                .targetGroup(targetGroup)
                .sentAt(LocalDateTime.now())
                .sentCount(0)
                .build();

        int sentCount = 0;
        int failedCount = 0;

        for (Member member : recipients) {
            try {
                // Format the message nicely
                String formattedMessage = formatMessage(
                        request.getTitle(),
                        request.getMessage(),
                        church.getName()
                );

                whatsAppService.sendMessage(member.getPhoneNumber(), formattedMessage);
                sentCount++;

                log.info("✅ Message sent to: {} ({})", member.getFullName(), member.getPhoneNumber());

            } catch (Exception e) {
                failedCount++;
                log.error("❌ Failed to send WhatsApp to {} ({}): {}",
                        member.getFullName(),
                        member.getPhoneNumber(),
                        e.getMessage());
            }
        }

        announcement.setSentCount(sentCount);
        announcement = announcementRepository.save(announcement);

        log.info("📊 Announcement Summary - Sent: {}, Failed: {}, Total: {}",
                sentCount, failedCount, recipients.size());

        return AnnouncementResponse.builder()
                .announcementId(announcement.getId())
                .sentCount(sentCount)
                .sentAt(announcement.getSentAt().toString())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<Announcement> getAnnouncements(Long churchId, Pageable pageable) {
        return announcementRepository.findByChurchIdWithChurch(churchId, pageable);
    }

    private List<Member> getRecipients(Long churchId, Group targetGroup) {
        if (targetGroup == null) {
            return memberRepository.findByChurchIdAndStatus(churchId, "ACTIVE");
        } else {
            return new ArrayList<>(targetGroup.getMembers());
        }
    }

    /**
     * Format message with nice styling for WhatsApp
     */
    private String formatMessage(String title, String message, String churchName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

        return "🔔 *" + title + "*\n\n" +
                message + "\n\n" +
                "━━━━━━━━━━━━━━━\n" +
                "📍 " + churchName + "\n" +
                "📅 " + timestamp + "\n" +
                "Stay blessed! 🙏";
    }
}