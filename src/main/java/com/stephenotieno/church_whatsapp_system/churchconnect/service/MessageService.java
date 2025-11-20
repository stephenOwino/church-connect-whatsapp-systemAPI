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
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;

    @Transactional
    public Message saveMessage(Long churchId, String phoneNumber, String direction,
                               String messageBody, String messageSid) {

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Member member = memberRepository.findByPhoneNumber(phoneNumber).orElse(null);

        // Get or create conversation
        Conversation conversation = conversationRepository
                .findByPhoneNumberAndChurchId(phoneNumber, churchId)
                .orElseGet(() -> createNewConversation(church, member, phoneNumber));

        // Create message
        Message message = Message.builder()
                .church(church)
                .member(member)
                .conversation(conversation)
                .phoneNumber(phoneNumber)
                .direction(direction)
                .messageBody(messageBody)
                .messageSid(messageSid)
                .messageType("TEXT")
                .status("SENT")
                .processed(false)
                .isCommand(false)
                .needsPastorReply(false)
                .build();

        message = messageRepository.save(message);

        // Update conversation
        updateConversation(conversation, messageBody, direction);

        log.info("💾 Message saved: {} from {}", message.getId(), phoneNumber);
        return message;
    }

    @Transactional
    public void markAsCommand(Long messageId, String commandType) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setIsCommand(true);
        message.setCommandType(commandType);
        message.setProcessed(true);
        messageRepository.save(message);
    }

    @Transactional
    public void markNeedsPastorReply(Long messageId, boolean needsReply) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        message.setNeedsPastorReply(needsReply);
        messageRepository.save(message);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getAllMessages(Long churchId, Pageable pageable) {
        return messageRepository.findByChurchIdOrderByCreatedAtDesc(churchId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessagesByMember(Long memberId, Pageable pageable) {
        return messageRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getMessagesByConversation(Long conversationId, Pageable pageable) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getRecentMessages(Long churchId, String phoneNumber, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return messageRepository.findRecentMessagesByPhoneNumber(churchId, phoneNumber, pageable)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MessageDTO getMessageById(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        return mapToDTO(message);
    }

    @Transactional
    public void updateMessageStatus(String messageSid, String status) {
        messageRepository.findByMessageSid(messageSid).ifPresent(message -> {
            message.setStatus(status);
            messageRepository.save(message);
            log.info("📊 Message status updated: {} -> {}", messageSid, status);
        });
    }

    private Conversation createNewConversation(Church church, Member member, String phoneNumber) {
        Conversation conversation = Conversation.builder()
                .church(church)
                .member(member)
                .phoneNumber(phoneNumber)
                .status("ACTIVE")
                .messageCount(0)
                .unreadCount(0)
                .build();

        return conversationRepository.save(conversation);
    }

    private void updateConversation(Conversation conversation, String lastMessage, String direction) {
        conversation.setLastMessage(lastMessage);
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setMessageCount(conversation.getMessageCount() + 1);

        if ("INBOUND".equals(direction)) {
            conversation.setUnreadCount(conversation.getUnreadCount() + 1);
        }

        conversationRepository.save(conversation);
    }

    private MessageDTO mapToDTO(Message message) {
        return MessageDTO.builder()
                .id(message.getId())
                .memberId(message.getMember() != null ? message.getMember().getId() : null)
                .memberName(message.getMember() != null ? message.getMember().getFullName() : "Unknown")
                .conversationId(message.getConversation() != null ? message.getConversation().getId() : null)
                .messageSid(message.getMessageSid())
                .phoneNumber(message.getPhoneNumber())
                .direction(message.getDirection())
                .messageBody(message.getMessageBody())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .status(message.getStatus())
                .isCommand(message.getIsCommand())
                .commandType(message.getCommandType())
                .processed(message.getProcessed())
                .needsPastorReply(message.getNeedsPastorReply())
                .errorMessage(message.getErrorMessage())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }
}