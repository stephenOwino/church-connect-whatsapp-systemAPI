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
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<ConversationDTO> getAllConversations(Long churchId, Pageable pageable) {
        return conversationRepository.findByChurchIdWithMember(churchId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ConversationDTO> getActiveConversations(Long churchId, Pageable pageable) {
        return conversationRepository.findByChurchIdAndStatusOrderByLastMessageAtDesc(
                        churchId, "ACTIVE", pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public ConversationDetailDTO getConversationById(Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Get recent messages
        Pageable pageable = PageRequest.of(0, 50);
        List<MessageDTO> messages = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(id, pageable)
                .getContent()
                .stream()
                .map(this::mapMessageToDTO)
                .collect(Collectors.toList());

        return ConversationDetailDTO.builder()
                .id(conversation.getId())
                .member(conversation.getMember() != null ?
                        MemberBasicDTO.builder()
                                .id(conversation.getMember().getId())
                                .fullName(conversation.getMember().getFullName())
                                .phoneNumber(conversation.getMember().getPhoneNumber())
                                .status(conversation.getMember().getStatus())
                                .build() : null)
                .phoneNumber(conversation.getPhoneNumber())
                .status(conversation.getStatus())
                .messageCount(conversation.getMessageCount())
                .unreadCount(conversation.getUnreadCount())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .messages(messages)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getUnreadConversations(Long churchId) {
        return conversationRepository.findUnreadConversations(churchId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setUnreadCount(0);
        conversationRepository.save(conversation);

        log.info("📖 Conversation {} marked as read", conversationId);
    }

    @Transactional
    public void archiveConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setStatus("ARCHIVED");
        conversationRepository.save(conversation);

        log.info("📦 Conversation {} archived", conversationId);
    }

    @Transactional
    public void closeConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setStatus("CLOSED");
        conversationRepository.save(conversation);

        log.info("✅ Conversation {} closed", conversationId);
    }

    @Transactional
    public void archiveInactiveConversations(Long churchId, int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<Conversation> inactiveConversations = conversationRepository
                .findInactiveConversations(churchId, cutoffDate);

        for (Conversation conversation : inactiveConversations) {
            conversation.setStatus("ARCHIVED");
        }

        conversationRepository.saveAll(inactiveConversations);
        log.info("📦 Archived {} inactive conversations", inactiveConversations.size());
    }

    @Transactional(readOnly = true)
    public Long countActiveConversations(Long churchId) {
        return conversationRepository.countActiveConversations(churchId);
    }

    private ConversationDTO mapToDTO(Conversation conversation) {
        return ConversationDTO.builder()
                .id(conversation.getId())
                .memberId(conversation.getMember() != null ? conversation.getMember().getId() : null)
                .memberName(conversation.getMember() != null ? conversation.getMember().getFullName() : "Unknown")
                .phoneNumber(conversation.getPhoneNumber())
                .status(conversation.getStatus())
                .lastMessage(conversation.getLastMessage())
                .lastMessageAt(conversation.getLastMessageAt())
                .messageCount(conversation.getMessageCount())
                .unreadCount(conversation.getUnreadCount())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private MessageDTO mapMessageToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .direction(message.getDirection())
                .messageBody(message.getMessageBody())
                .messageType(message.getMessageType())
                .status(message.getStatus())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
