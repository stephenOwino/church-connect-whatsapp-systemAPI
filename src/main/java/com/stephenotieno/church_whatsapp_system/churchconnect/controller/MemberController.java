package com.stephenotieno.church_whatsapp_system.churchconnect.controller;


import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MemberRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MemberResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<MemberResponse>>> getAllMembers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<MemberResponse> members = memberService.getAllMembers(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<MemberResponse>>builder()
                .success(true)
                .message("Members retrieved successfully")
                .data(members)
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> addMember(
            HttpServletRequest request,
            @Valid @RequestBody MemberRequest memberRequest) {

        Long churchId = extractChurchId(request);
        MemberResponse response = memberService.addMember(churchId, memberRequest);

        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .success(true)
                .message("Member added successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> getMemberById(@PathVariable Long id) {
        MemberResponse response = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponse>> updateMember(
            @PathVariable Long id,
            @Valid @RequestBody MemberRequest memberRequest) {

        MemberResponse response = memberService.updateMember(id, memberRequest);
        return ResponseEntity.ok(ApiResponse.<MemberResponse>builder()
                .success(true)
                .message("Member updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deactivateMember(@PathVariable Long id) {
        memberService.deactivateMember(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Member deactivated successfully")
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMemberStats(HttpServletRequest request) {
        Long churchId = extractChurchId(request);
        Map<String, Object> stats = memberService.getMemberStats(churchId);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .data(stats)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }
}