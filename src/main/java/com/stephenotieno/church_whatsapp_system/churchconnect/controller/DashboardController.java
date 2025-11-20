package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.DashboardStatsDTO;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.DashboardService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtUtil jwtUtil;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats(
            HttpServletRequest request) {

        Long churchId = extractChurchId(request);
        DashboardStatsDTO stats = dashboardService.getDashboardStats(churchId);

        return ResponseEntity.ok(ApiResponse.<DashboardStatsDTO>builder()
                .success(true)
                .message("Dashboard stats retrieved successfully")
                .data(stats)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}