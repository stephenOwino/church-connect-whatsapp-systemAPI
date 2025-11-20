package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopContributorDTO {
    private Long memberId;
    private String memberName;
    private BigDecimal totalAmount;
    private Long offeringCount;
}
