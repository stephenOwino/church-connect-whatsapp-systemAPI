package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageActivityDTO {
    private String date;
    private Long inboundCount;
    private Long outboundCount;
    private Long totalCount;
}
