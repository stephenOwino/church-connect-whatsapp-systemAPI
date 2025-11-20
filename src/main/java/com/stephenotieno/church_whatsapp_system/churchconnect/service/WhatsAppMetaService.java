package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import jakarta.annotation.PostConstruct;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppMetaService {

    @Value("${meta.whatsapp.phone.number.id}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.access.token}")
    private String accessToken;

    @Value("${meta.whatsapp.api.version:v21.0}")
    private String apiVersion;

    private String baseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        this.baseUrl = "https://graph.facebook.com/" + apiVersion + "/" + phoneNumberId;
        log.info("✅ Meta WhatsApp Service initialized");
        log.info("📱 Phone Number ID: {}", phoneNumberId);
        log.info("🌐 Base URL: {}", baseUrl);
    }

    /**
     * Send a text message via Meta WhatsApp Business API
     */
    public String sendMessage(String to, String messageBody) {
        try {
            // Remove 'whatsapp:' prefix if present
            String cleanNumber = to.replace("whatsapp:", "").trim();

            // Ensure number starts with + or country code
            if (!cleanNumber.startsWith("+")) {
                cleanNumber = "+" + cleanNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", cleanNumber);
            requestBody.put("type", "text");

            Map<String, String> text = new HashMap<>();
            text.put("preview_url", "false");
            text.put("body", messageBody);
            requestBody.put("text", text);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/messages";
            log.info("📤 Sending message to {} via Meta API", cleanNumber);
            log.debug("Request URL: {}", url);
            log.debug("Request Body: {}", requestBody);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = (String) messages.get(0).get("id");
                    log.info("✅ Message sent successfully to {}: ID={}", cleanNumber, messageId);
                    return messageId;
                }
            }

            log.warn("⚠️ Unexpected response format from Meta API");
            return null;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("❌ HTTP Error sending message to {}: {} - {}",
                    to, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to send message to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp message: " + e.getMessage());
        }
    }

    /**
     * Send a message with media (image, document, etc.)
     */
    public String sendMessageWithMedia(String to, String caption, String mediaUrl, String mediaType) {
        try {
            String cleanNumber = to.replace("whatsapp:", "").trim();
            if (!cleanNumber.startsWith("+")) {
                cleanNumber = "+" + cleanNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", cleanNumber);
            requestBody.put("type", mediaType); // image, document, video, audio

            Map<String, Object> media = new HashMap<>();
            media.put("link", mediaUrl);
            if (caption != null && !caption.isEmpty()) {
                media.put("caption", caption);
            }
            requestBody.put(mediaType, media);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/messages";
            log.info("📤 Sending {} media to {}", mediaType, cleanNumber);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = (String) messages.get(0).get("id");
                    log.info("✅ Media message sent successfully: ID={}", messageId);
                    return messageId;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Failed to send media message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send WhatsApp media message: " + e.getMessage());
        }
    }

    /**
     * Send an interactive button message
     */
    public String sendButtonMessage(String to, String bodyText, List<ButtonData> buttons) {
        try {
            String cleanNumber = to.replace("whatsapp:", "").trim();
            if (!cleanNumber.startsWith("+")) {
                cleanNumber = "+" + cleanNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", cleanNumber);
            requestBody.put("type", "interactive");

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "button");

            Map<String, String> body = new HashMap<>();
            body.put("text", bodyText);
            interactive.put("body", body);

            List<Map<String, Object>> actionButtons = new ArrayList<>();
            for (ButtonData button : buttons) {
                Map<String, Object> btn = new HashMap<>();
                btn.put("type", "reply");

                Map<String, String> reply = new HashMap<>();
                reply.put("id", button.getId());
                reply.put("title", button.getTitle());
                btn.put("reply", reply);

                actionButtons.add(btn);
            }

            Map<String, Object> action = new HashMap<>();
            action.put("buttons", actionButtons);
            interactive.put("action", action);

            requestBody.put("interactive", interactive);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/messages";
            log.info("📤 Sending button message to {}", cleanNumber);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = (String) messages.get(0).get("id");
                    log.info("✅ Button message sent successfully: ID={}", messageId);
                    return messageId;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Failed to send button message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send button message: " + e.getMessage());
        }
    }

    /**
     * Send an interactive list message
     */
    public String sendListMessage(String to, String bodyText, String buttonText,
                                  List<ListSection> sections) {
        try {
            String cleanNumber = to.replace("whatsapp:", "").trim();
            if (!cleanNumber.startsWith("+")) {
                cleanNumber = "+" + cleanNumber;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("recipient_type", "individual");
            requestBody.put("to", cleanNumber);
            requestBody.put("type", "interactive");

            Map<String, Object> interactive = new HashMap<>();
            interactive.put("type", "list");

            Map<String, String> body = new HashMap<>();
            body.put("text", bodyText);
            interactive.put("body", body);

            Map<String, Object> action = new HashMap<>();
            action.put("button", buttonText);

            List<Map<String, Object>> sectionsList = new ArrayList<>();
            for (ListSection section : sections) {
                Map<String, Object> sec = new HashMap<>();
                sec.put("title", section.getTitle());

                List<Map<String, Object>> rows = new ArrayList<>();
                for (ListRow row : section.getRows()) {
                    Map<String, Object> r = new HashMap<>();
                    r.put("id", row.getId());
                    r.put("title", row.getTitle());
                    if (row.getDescription() != null) {
                        r.put("description", row.getDescription());
                    }
                    rows.add(r);
                }
                sec.put("rows", rows);
                sectionsList.add(sec);
            }
            action.put("sections", sectionsList);
            interactive.put("action", action);

            requestBody.put("interactive", interactive);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/messages";
            log.info("📤 Sending list message to {}", cleanNumber);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("messages");

                if (messages != null && !messages.isEmpty()) {
                    String messageId = (String) messages.get(0).get("id");
                    log.info("✅ List message sent successfully: ID={}", messageId);
                    return messageId;
                }
            }

            return null;

        } catch (Exception e) {
            log.error("❌ Failed to send list message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send list message: " + e.getMessage());
        }
    }

    /**
     * Mark message as read
     */
    public boolean markAsRead(String messageId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messaging_product", "whatsapp");
            requestBody.put("status", "read");
            requestBody.put("message_id", messageId);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = baseUrl + "/messages";
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("📖 Message {} marked as read", messageId);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("❌ Failed to mark message as read: {}", e.getMessage());
            return false;
        }
    }

    // Helper classes for interactive messages
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ButtonData {
        private String id;
        private String title;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ListSection {
        private String title;
        private List<ListRow> rows;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ListRow {
        private String id;
        private String title;
        private String description;
    }
}