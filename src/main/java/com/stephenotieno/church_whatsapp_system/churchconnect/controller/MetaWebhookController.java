package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/whatsapp")
@RequiredArgsConstructor
@Slf4j
public class MetaWebhookController {

    private final ChatbotService chatbotService;

    @Value("${meta.whatsapp.webhook.verify.token}")
    private String verifyToken;

    /**
     * Webhook verification endpoint (GET) - Required by Meta
     */
    @GetMapping("/webhook")
    public ResponseEntity<?> verifyWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String token,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        log.info("🔍 Webhook verification request received");
        log.info("Mode: {}, Token: {}", mode, token);

        if ("subscribe".equals(mode) && verifyToken.equals(token)) {
            log.info("✅ Webhook verified successfully");
            return ResponseEntity.ok(challenge);
        }

        log.warn("❌ Webhook verification failed - Invalid token");
        return ResponseEntity.status(403).body("Forbidden");
    }

    /**
     * Main webhook endpoint to receive incoming WhatsApp messages from Meta
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> receiveMessage(@RequestBody Map<String, Object> payload) {
        log.info("📨 ========================================");
        log.info("📨 Received Meta WhatsApp webhook:");
        log.debug("📨 Payload: {}", payload);
        log.info("📨 ========================================");

        try {
            // Parse Meta webhook structure
            String object = (String) payload.get("object");

            if (!"whatsapp_business_account".equals(object)) {
                log.warn("⚠️ Unexpected object type: {}", object);
                return ResponseEntity.ok("Event received");
            }

            List<Map<String, Object>> entries = (List<Map<String, Object>>) payload.get("entry");

            if (entries == null || entries.isEmpty()) {
                log.warn("⚠️ No entries in webhook");
                return ResponseEntity.ok("No entries");
            }

            for (Map<String, Object> entry : entries) {
                List<Map<String, Object>> changes = (List<Map<String, Object>>) entry.get("changes");

                if (changes == null) continue;

                for (Map<String, Object> change : changes) {
                    Map<String, Object> value = (Map<String, Object>) change.get("value");

                    if (value == null) continue;

                    // Check for messages
                    List<Map<String, Object>> messages = (List<Map<String, Object>>) value.get("messages");

                    if (messages != null && !messages.isEmpty()) {
                        processMessages(messages);
                    }

                    // Check for status updates
                    List<Map<String, Object>> statuses = (List<Map<String, Object>>) value.get("statuses");

                    if (statuses != null && !statuses.isEmpty()) {
                        processStatuses(statuses);
                    }
                }
            }

            return ResponseEntity.ok("EVENT_RECEIVED");

        } catch (Exception e) {
            log.error("❌ Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("ERROR_PROCESSED");
        }
    }

    /**
     * Process incoming messages
     */
    private void processMessages(List<Map<String, Object>> messages) {
        for (Map<String, Object> message : messages) {
            try {
                String messageId = (String) message.get("id");
                String from = (String) message.get("from");
                String timestamp = (String) message.get("timestamp");
                String type = (String) message.get("type");

                log.info("📱 Processing message from: {}", from);
                log.info("🆔 Message ID: {}", messageId);
                log.info("📝 Type: {}", type);

                String messageBody = null;

                // Extract message body based on type
                if ("text".equals(type)) {
                    Map<String, Object> text = (Map<String, Object>) message.get("text");
                    if (text != null) {
                        messageBody = (String) text.get("body");
                    }
                } else if ("button".equals(type)) {
                    Map<String, Object> button = (Map<String, Object>) message.get("button");
                    if (button != null) {
                        messageBody = (String) button.get("text");
                    }
                } else if ("interactive".equals(type)) {
                    Map<String, Object> interactive = (Map<String, Object>) message.get("interactive");
                    if (interactive != null) {
                        String interactiveType = (String) interactive.get("type");
                        if ("button_reply".equals(interactiveType)) {
                            Map<String, Object> buttonReply = (Map<String, Object>) interactive.get("button_reply");
                            if (buttonReply != null) {
                                messageBody = (String) buttonReply.get("title");
                            }
                        } else if ("list_reply".equals(interactiveType)) {
                            Map<String, Object> listReply = (Map<String, Object>) interactive.get("list_reply");
                            if (listReply != null) {
                                messageBody = (String) listReply.get("title");
                            }
                        }
                    }
                }

                if (messageBody == null || messageBody.isEmpty()) {
                    log.warn("⚠️ No message body found or unsupported message type: {}", type);
                    continue;
                }

                log.info("💬 Message: {}", messageBody);

                // Process through chatbot service
                chatbotService.processIncomingMessage("whatsapp:" + from, messageBody, messageId);

                log.info("✅ Message processed successfully");

            } catch (Exception e) {
                log.error("❌ Error processing individual message: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Process status updates (delivered, read, failed, etc.)
     */
    private void processStatuses(List<Map<String, Object>> statuses) {
        for (Map<String, Object> status : statuses) {
            try {
                String messageId = (String) status.get("id");
                String statusValue = (String) status.get("status");
                String recipient = (String) status.get("recipient_id");

                log.info("📊 Status Update:");
                log.info("   Message ID: {}", messageId);
                log.info("   Status: {}", statusValue);
                log.info("   Recipient: {}", recipient);

                // Handle errors
                if ("failed".equals(statusValue)) {
                    List<Map<String, Object>> errors = (List<Map<String, Object>>) status.get("errors");
                    if (errors != null && !errors.isEmpty()) {
                        Map<String, Object> error = errors.get(0);
                        log.error("❌ Message failed: Code={}, Title={}",
                                error.get("code"), error.get("title"));
                    }
                }

                // TODO: Update message status in database

            } catch (Exception e) {
                log.error("❌ Error processing status: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testWebhook() {
        log.info("🧪 Test endpoint called");

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Meta WhatsApp webhook is running",
                "timestamp", System.currentTimeMillis(),
                "provider", "Meta WhatsApp Business API",
                "endpoints", Map.of(
                        "webhook", "/api/whatsapp/webhook",
                        "test", "/api/whatsapp/test"
                )
        ));
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "Meta WhatsApp Webhook",
                "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}
