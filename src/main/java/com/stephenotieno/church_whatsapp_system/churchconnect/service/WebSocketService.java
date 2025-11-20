package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.WebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendToChurch(Long churchId, String type, String action, Object payload) {
        WebSocketMessage message = WebSocketMessage.builder()
                .type(type)
                .action(action)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .churchId(churchId)
                .build();

        String destination = "/topic/church/" + churchId;
        messagingTemplate.convertAndSend(destination, message);

        log.info("📡 WebSocket message sent: {} - {} to church {}", type, action, churchId);
    }

    public void notifyNewMessage(Long churchId, Object messageData) {
        sendToChurch(churchId, "NEW_MESSAGE", "CREATE", messageData);
    }

    public void notifyNewMember(Long churchId, Object memberData) {
        sendToChurch(churchId, "NEW_MEMBER", "CREATE", memberData);
    }

    public void notifyPastorQueue(Long churchId, Object queueData) {
        sendToChurch(churchId, "PASTOR_QUEUE", "CREATE", queueData);
    }

    public void notifyNewOffering(Long churchId, Object offeringData) {
        sendToChurch(churchId, "OFFERING", "CREATE", offeringData);
    }

    public void notifyDashboardUpdate(Long churchId, Object statsData) {
        sendToChurch(churchId, "DASHBOARD", "UPDATE", statsData);
    }
}