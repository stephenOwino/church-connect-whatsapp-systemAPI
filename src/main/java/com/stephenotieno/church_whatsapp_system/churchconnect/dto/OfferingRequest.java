package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfferingRequest {
    private Long memberId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be at least 1")
    private BigDecimal amount;

    private String paymentMethod = "MPESA";
    private String transactionCode;
}