package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AnnouncementDTO;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AnnouncementRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AnnouncementResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ChurchBasicDTO;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Announcement;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<ApiResponse<AnnouncementResponse>> sendAnnouncement(
            HttpServletRequest request,
            @Valid @RequestBody AnnouncementRequest announcementRequest) {

        Long churchId = extractChurchId(request);
        AnnouncementResponse response = announcementService.sendAnnouncement(churchId, announcementRequest);

        return ResponseEntity.ok(ApiResponse.<AnnouncementResponse>builder()
                .success(true)
                .message("Announcement sent to " + response.getSentCount() + " members")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AnnouncementDTO>>> getAnnouncements(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);

        // Get announcements from service (church is loaded via JOIN FETCH)
        Page<Announcement> announcements = announcementService.getAnnouncements(churchId, pageable);

        // Convert entities to DTOs to avoid lazy loading and circular reference issues
        Page<AnnouncementDTO> announcementDTOs = announcements.map(announcement ->
                AnnouncementDTO.builder()
                        .id(announcement.getId())
                        .title(announcement.getTitle())
                        .message(announcement.getMessage())
                        .church(ChurchBasicDTO.builder()
                                .id(announcement.getChurch().getId())
                                .name(announcement.getChurch().getName())
                                .location(announcement.getChurch().getLocation())
                                .build())
                        .sentAt(announcement.getSentAt())
                        .sentCount(announcement.getSentCount())
                        .createdAt(announcement.getCreatedAt())
                        .build()
        );

        return ResponseEntity.ok(ApiResponse.<Page<AnnouncementDTO>>builder()
                .success(true)
                .data(announcementDTOs)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}