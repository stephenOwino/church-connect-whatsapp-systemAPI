package com.stephenotieno.church_whatsapp_system.churchconnect.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberGrowthDTO {
    private String date;
    private Long newMembers;
}