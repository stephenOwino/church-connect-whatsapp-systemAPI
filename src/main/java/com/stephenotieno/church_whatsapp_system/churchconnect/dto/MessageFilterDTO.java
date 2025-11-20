package com.stephenotieno.church_whatsapp_system.churchconnect.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageFilterDTO {
    private String direction; // INBOUND, OUTBOUND
    private String status;
    private Boolean isCommand;
    private String commandType;
    private Boolean needsPastorReply;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String phoneNumber;
}