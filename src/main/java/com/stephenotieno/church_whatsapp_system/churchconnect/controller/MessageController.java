package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.MessageService;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.WhatsAppMetaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final WhatsAppMetaService whatsAppMetaService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getAllMessages(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MessageDTO> messages = messageService.getAllMessages(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<MessageDTO>>builder()
                .success(true)
                .message("Messages retrieved successfully")
                .data(messages)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MessageDTO>> getMessageById(@PathVariable Long id) {
        MessageDTO message = messageService.getMessageById(id);

        return ResponseEntity.ok(ApiResponse.<MessageDTO>builder()
                .success(true)
                .data(message)
                .build());
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<ApiResponse<Page<MessageDTO>>> getMessagesByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageDTO> messages = messageService.getMessagesByMember(memberId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<MessageDTO>>builder()
                .success(true)
                .data(messages)
                .build());
    }

    @GetMapping("/recent/{phoneNumber}")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getRecentMessages(
            HttpServletRequest request,
            @PathVariable String phoneNumber,
            @RequestParam(defaultValue = "10") int limit) {

        Long churchId = extractChurchId(request);
        List<MessageDTO> messages = messageService.getRecentMessages(churchId, phoneNumber, limit);

        return ResponseEntity.ok(ApiResponse.<List<MessageDTO>>builder()
                .success(true)
                .data(messages)
                .build());
    }

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            HttpServletRequest request,
            @Valid @RequestBody MessageRequest messageRequest) {

        Long churchId = extractChurchId(request);

        try {
            // Send via Meta WhatsApp
            String messageSid = whatsAppMetaService.sendMessage(
                    messageRequest.getPhoneNumber(),
                    messageRequest.getMessageBody()
            );

            // Save outbound message
            messageService.saveMessage(
                    churchId,
                    messageRequest.getPhoneNumber(),
                    "OUTBOUND",
                    messageRequest.getMessageBody(),
                    messageSid
            );

            MessageResponse response = MessageResponse.builder()
                    .messageSid(messageSid)
                    .phoneNumber(messageRequest.getPhoneNumber())
                    .status("SENT")
                    .message("Message sent successfully")
                    .success(true)
                    .build();

            return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                    .success(true)
                    .message("Message sent successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                    .success(false)
                    .message("Failed to send message: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-media")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMediaMessage(
            HttpServletRequest request,
            @Valid @RequestBody MessageRequest messageRequest) {

        Long churchId = extractChurchId(request);

        try {
            String messageSid = whatsAppMetaService.sendMessageWithMedia(
                    messageRequest.getPhoneNumber(),
                    messageRequest.getMessageBody(),
                    messageRequest.getMediaUrl(),
                    messageRequest.getMessageType() != null ? messageRequest.getMessageType() : "image"
            );

            messageService.saveMessage(
                    churchId,
                    messageRequest.getPhoneNumber(),
                    "OUTBOUND",
                    messageRequest.getMessageBody(),
                    messageSid
            );

            MessageResponse response = MessageResponse.builder()
                    .messageSid(messageSid)
                    .phoneNumber(messageRequest.getPhoneNumber())
                    .status("SENT")
                    .message("Media message sent successfully")
                    .success(true)
                    .build();

            return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                    .success(true)
                    .message("Media message sent successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MessageResponse>builder()
                    .success(false)
                    .message("Failed to send media message: " + e.getMessage())
                    .build());
        }
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}