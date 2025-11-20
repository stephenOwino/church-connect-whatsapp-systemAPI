package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OfferingTrendDTO {
    private String month;
    private Integer year;
    private BigDecimal totalAmount;
    private Long offeringCount;
}
