package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PastorQueueService {

    private final PastorQueueRepository pastorQueueRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;
    private final AdminRepository adminRepository;
    private final ChurchRepository churchRepository;
    private final WhatsAppMetaService whatsAppMetaService;

    @Transactional
    public PastorQueueDTO addToQueue(Long churchId, PastorQueueRequest request) {
        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        PastorQueue queue = PastorQueue.builder()
                .message(message)
                .member(message.getMember())
                .church(church)
                .phoneNumber(message.getPhoneNumber())
                .priority(request.getPriority() != null ? request.getPriority() : "MEDIUM")
                .category(request.getCategory())
                .status("PENDING")
                .assignedTo(request.getAssignedTo())
                .notes(request.getNotes())
                .build();

        queue = pastorQueueRepository.save(queue);

        // Mark message as needing pastor reply
        message.setNeedsPastorReply(true);
        messageRepository.save(message);

        log.info("📬 Message added to pastor queue: {}", queue.getId());
        return mapToDTO(queue);
    }

    @Transactional(readOnly = true)
    public Page<PastorQueueDTO> getPendingQueue(Long churchId, Pageable pageable) {
        return pastorQueueRepository.findByChurchIdAndStatusWithDetails(
                        churchId, "PENDING", pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PastorQueueDTO> getAllQueue(Long churchId, Pageable pageable) {
        return pastorQueueRepository.findByChurchIdOrderByCreatedAtDesc(churchId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PastorQueueDTO> getQueueByStatus(Long churchId, String status, Pageable pageable) {
        return pastorQueueRepository.findByChurchIdAndStatusOrderByCreatedAtDesc(
                        churchId, status, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PastorQueueDTO> getQueueByCategory(Long churchId, String category, Pageable pageable) {
        return pastorQueueRepository.findByChurchIdAndCategoryOrderByCreatedAtDesc(
                        churchId, category, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<PastorQueueDTO> getQueueByPriority(Long churchId, String priority, Pageable pageable) {
        return pastorQueueRepository.findByChurchIdAndPriorityOrderByCreatedAtDesc(
                        churchId, priority, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<PastorQueueDTO> getAssignedToMe(Long adminId) {
        return pastorQueueRepository.findByAssignedToOrderByCreatedAtDesc(adminId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PastorQueueDTO replyToQueue(Long adminId, PastorReplyRequest request) {
        PastorQueue queue = pastorQueueRepository.findById(request.getQueueId())
                .orElseThrow(() -> new RuntimeException("Queue item not found"));

        // Send WhatsApp reply
        whatsAppMetaService.sendMessage(queue.getPhoneNumber(), request.getReplyMessage());

        // Update queue
        queue.setPastorReply(request.getReplyMessage());
        queue.setRepliedAt(LocalDateTime.now());
        queue.setRepliedBy(adminId);
        queue.setStatus(request.getStatus() != null ? request.getStatus() : "REPLIED");
        if (request.getNotes() != null) {
            queue.setNotes(request.getNotes());
        }

        queue = pastorQueueRepository.save(queue);

        log.info("✉️ Pastor reply sent for queue: {}", queue.getId());
        return mapToDTO(queue);
    }

    @Transactional
    public void assignToAdmin(Long queueId, Long adminId) {
        PastorQueue queue = pastorQueueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue item not found"));

        queue.setAssignedTo(adminId);
        queue.setStatus("ASSIGNED");
        pastorQueueRepository.save(queue);

        log.info("👤 Queue {} assigned to admin {}", queueId, adminId);
    }

    @Transactional
    public void updatePriority(Long queueId, String priority) {
        PastorQueue queue = pastorQueueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue item not found"));

        queue.setPriority(priority);
        pastorQueueRepository.save(queue);

        log.info("⚡ Queue {} priority updated to {}", queueId, priority);
    }

    @Transactional
    public void closeQueue(Long queueId) {
        PastorQueue queue = pastorQueueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue item not found"));

        queue.setStatus("CLOSED");
        pastorQueueRepository.save(queue);

        log.info("✅ Queue {} closed", queueId);
    }

    @Transactional(readOnly = true)
    public Long countPending(Long churchId) {
        return pastorQueueRepository.countPendingByChurchId(churchId);
    }

    @Transactional(readOnly = true)
    public Long countAssignedToMe(Long adminId) {
        return pastorQueueRepository.countAssignedToAdmin(adminId);
    }

    private PastorQueueDTO mapToDTO(PastorQueue queue) {
        return PastorQueueDTO.builder()
                .id(queue.getId())
                .messageId(queue.getMessage().getId())
                .memberId(queue.getMember() != null ? queue.getMember().getId() : null)
                .memberName(queue.getMember() != null ? queue.getMember().getFullName() : "Unknown")
                .phoneNumber(queue.getPhoneNumber())
                .messageBody(queue.getMessage().getMessageBody())
                .priority(queue.getPriority())
                .category(queue.getCategory())
                .status(queue.getStatus())
                .assignedTo(queue.getAssignedTo())
                .assignedToName(getAdminName(queue.getAssignedTo()))
                .pastorReply(queue.getPastorReply())
                .repliedAt(queue.getRepliedAt())
                .repliedByName(getAdminName(queue.getRepliedBy()))
                .notes(queue.getNotes())
                .createdAt(queue.getCreatedAt())
                .updatedAt(queue.getUpdatedAt())
                .build();
    }

    private String getAdminName(Long adminId) {
        if (adminId == null) return null;
        return adminRepository.findById(adminId)
                .map(Admin::getFullName)
                .orElse("Unknown");
    }
}