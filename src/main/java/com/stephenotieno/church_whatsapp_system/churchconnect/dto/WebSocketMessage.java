package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type; // NEW_MESSAGE, NEW_MEMBER, PASTOR_QUEUE, OFFERING
    private String action; // CREATE, UPDATE, DELETE
    private Object payload;
    private LocalDateTime timestamp;
    private Long churchId;
}