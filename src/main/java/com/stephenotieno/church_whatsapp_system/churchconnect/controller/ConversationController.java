package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.ConversationService;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ConversationDTO>>> getAllConversations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<ConversationDTO> conversations = conversationService.getAllConversations(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<ConversationDTO>>builder()
                .success(true)
                .message("Conversations retrieved successfully")
                .data(conversations)
                .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<Page<ConversationDTO>>> getActiveConversations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<ConversationDTO> conversations = conversationService.getActiveConversations(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<ConversationDTO>>builder()
                .success(true)
                .data(conversations)
                .build());
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<ConversationDTO>>> getUnreadConversations(
            HttpServletRequest request) {

        Long churchId = extractChurchId(request);
        List<ConversationDTO> conversations = conversationService.getUnreadConversations(churchId);

        return ResponseEntity.ok(ApiResponse.<List<ConversationDTO>>builder()
                .success(true)
                .data(conversations)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConversationDetailDTO>> getConversationById(
            @PathVariable Long id) {

        ConversationDetailDTO conversation = conversationService.getConversationById(id);

        return ResponseEntity.ok(ApiResponse.<ConversationDetailDTO>builder()
                .success(true)
                .data(conversation)
                .build());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getConversationMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageDTO> messages = messageService.getMessagesByConversation(id, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<MessageDTO>>builder()
                .success(true)
                .data(messages)
                .build());
    }

    @PutMapping("/{id}/mark-read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        conversationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Conversation marked as read")
                .build());
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveConversation(@PathVariable Long id) {
        conversationService.archiveConversation(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Conversation archived")
                .build());
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeConversation(@PathVariable Long id) {
        conversationService.closeConversation(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Conversation closed")
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Long>> getActiveCount(HttpServletRequest request) {
        Long churchId = extractChurchId(request);
        Long count = conversationService.countActiveConversations(churchId);

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .data(count)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}