package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.AnalyticsService;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.CommandService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CommandService commandService;
    private final JwtUtil jwtUtil;

    @PostMapping("/generate-today")
    public ResponseEntity<ApiResponse<Void>> generateTodayAnalytics(HttpServletRequest request) {
        Long churchId = extractChurchId(request);
        analyticsService.generateTodayAnalytics(churchId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Today's analytics generated successfully")
                .build());
    }

    @PostMapping("/generate/{date}")
    public ResponseEntity<ApiResponse<Void>> generateAnalyticsForDate(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long churchId = extractChurchId(request);
        analyticsService.generateDailyAnalytics(churchId, date);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Analytics generated for " + date)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnalyticsDTO>>> getAnalytics(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long churchId = extractChurchId(request);
        List<AnalyticsDTO> analytics = analyticsService.getAnalytics(churchId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<List<AnalyticsDTO>>builder()
                .success(true)
                .data(analytics)
                .build());
    }

    @GetMapping("/recent/{days}")
    public ResponseEntity<ApiResponse<List<AnalyticsDTO>>> getRecentAnalytics(
            HttpServletRequest request,
            @PathVariable int days) {

        Long churchId = extractChurchId(request);
        List<AnalyticsDTO> analytics = analyticsService.getRecentAnalytics(churchId, days);

        return ResponseEntity.ok(ApiResponse.<List<AnalyticsDTO>>builder()
                .success(true)
                .data(analytics)
                .build());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<AnalyticsDTO>> getAnalyticsForDate(
            HttpServletRequest request,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Long churchId = extractChurchId(request);
        AnalyticsDTO analytics = analyticsService.getAnalyticsForDate(churchId, date);

        return ResponseEntity.ok(ApiResponse.<AnalyticsDTO>builder()
                .success(true)
                .data(analytics)
                .build());
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getSummaryStats(
            HttpServletRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Long churchId = extractChurchId(request);
        Map<String, Long> stats = analyticsService.getSummaryStats(churchId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder()
                .success(true)
                .data(stats)
                .build());
    }

    @GetMapping("/commands/stats")
    public ResponseEntity<ApiResponse<CommandStatsDTO>> getCommandStats(HttpServletRequest request) {
        Long churchId = extractChurchId(request);
        CommandStatsDTO stats = commandService.getCommandStats(churchId);

        return ResponseEntity.ok(ApiResponse.<CommandStatsDTO>builder()
                .success(true)
                .data(stats)
                .build());
    }

    @GetMapping("/commands/usage/{days}")
    public ResponseEntity<ApiResponse<List<CommandUsageDTO>>> getCommandUsage(
            HttpServletRequest request,
            @PathVariable int days) {

        Long churchId = extractChurchId(request);
        List<CommandUsageDTO> usage = commandService.getCommandUsageStats(churchId, days);

        return ResponseEntity.ok(ApiResponse.<List<CommandUsageDTO>>builder()
                .success(true)
                .data(usage)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}