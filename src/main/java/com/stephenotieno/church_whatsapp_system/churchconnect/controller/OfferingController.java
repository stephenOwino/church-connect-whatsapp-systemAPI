package com.stephenotieno.church_whatsapp_system.churchconnect.controller;


import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.OfferingRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.OfferingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/offerings")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Offering>>> getOfferings(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<Offering> offerings = offeringService.getOfferings(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<Offering>>builder()
                .success(true)
                .data(offerings)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Offering>> recordOffering(
            HttpServletRequest request,
            @Valid @RequestBody OfferingRequest offeringRequest) {

        Long churchId = extractChurchId(request);
        Offering offering = offeringService.recordOffering(churchId, offeringRequest);

        return ResponseEntity.ok(ApiResponse.<Offering>builder()
                .success(true)
                .message("Offering recorded successfully")
                .data(offering)
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOfferingStats(HttpServletRequest request) {
        Long churchId = extractChurchId(request);
        Map<String, Object> stats = offeringService.getOfferingStats(churchId);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(stats)
                .build());
    }

    @PostMapping("/mpesa-callback")
    public ResponseEntity<String> handleMpesaCallback(@RequestBody Map<String, Object> callbackData) {
        offeringService.handleMpesaCallback(callbackData);
        return ResponseEntity.ok("Callback processed");
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}