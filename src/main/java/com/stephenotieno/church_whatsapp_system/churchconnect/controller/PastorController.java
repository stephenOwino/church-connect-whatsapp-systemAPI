package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.PastorQueueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pastor")
@RequiredArgsConstructor
public class PastorController {

    private final PastorQueueService pastorQueueService;
    private final JwtUtil jwtUtil;

    @PostMapping("/queue")
    public ResponseEntity<ApiResponse<PastorQueueDTO>> addToQueue(
            HttpServletRequest request,
            @Valid @RequestBody PastorQueueRequest queueRequest) {

        Long churchId = extractChurchId(request);
        PastorQueueDTO queue = pastorQueueService.addToQueue(churchId, queueRequest);

        return ResponseEntity.ok(ApiResponse.<PastorQueueDTO>builder()
                .success(true)
                .message("Message added to pastor queue")
                .data(queue)
                .build());
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse<Page<PastorQueueDTO>>> getAllQueue(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<PastorQueueDTO> queue = pastorQueueService.getAllQueue(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @GetMapping("/queue/pending")
    public ResponseEntity<ApiResponse<Page<PastorQueueDTO>>> getPendingQueue(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<PastorQueueDTO> queue = pastorQueueService.getPendingQueue(churchId, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @GetMapping("/queue/status/{status}")
    public ResponseEntity<ApiResponse<Page<PastorQueueDTO>>> getQueueByStatus(
            HttpServletRequest request,
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<PastorQueueDTO> queue = pastorQueueService.getQueueByStatus(churchId, status, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @GetMapping("/queue/category/{category}")
    public ResponseEntity<ApiResponse<Page<PastorQueueDTO>>> getQueueByCategory(
            HttpServletRequest request,
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<PastorQueueDTO> queue = pastorQueueService.getQueueByCategory(churchId, category, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @GetMapping("/queue/priority/{priority}")
    public ResponseEntity<ApiResponse<Page<PastorQueueDTO>>> getQueueByPriority(
            HttpServletRequest request,
            @PathVariable String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long churchId = extractChurchId(request);
        Pageable pageable = PageRequest.of(page, size);
        Page<PastorQueueDTO> queue = pastorQueueService.getQueueByPriority(churchId, priority, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @GetMapping("/queue/assigned-to-me")
    public ResponseEntity<ApiResponse<List<PastorQueueDTO>>> getAssignedToMe(
            HttpServletRequest request) {

        Long adminId = extractAdminId(request);
        List<PastorQueueDTO> queue = pastorQueueService.getAssignedToMe(adminId);

        return ResponseEntity.ok(ApiResponse.<List<PastorQueueDTO>>builder()
                .success(true)
                .data(queue)
                .build());
    }

    @PostMapping("/reply")
    public ResponseEntity<ApiResponse<PastorQueueDTO>> replyToQueue(
            HttpServletRequest request,
            @Valid @RequestBody PastorReplyRequest replyRequest) {

        Long adminId = extractAdminId(request);
        PastorQueueDTO queue = pastorQueueService.replyToQueue(adminId, replyRequest);

        return ResponseEntity.ok(ApiResponse.<PastorQueueDTO>builder()
                .success(true)
                .message("Reply sent successfully")
                .data(queue)
                .build());
    }

    @PutMapping("/queue/{id}/assign/{adminId}")
    public ResponseEntity<ApiResponse<Void>> assignToAdmin(
            @PathVariable Long id,
            @PathVariable Long adminId) {

        pastorQueueService.assignToAdmin(id, adminId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Queue item assigned successfully")
                .build());
    }

    @PutMapping("/queue/{id}/priority/{priority}")
    public ResponseEntity<ApiResponse<Void>> updatePriority(
            @PathVariable Long id,
            @PathVariable String priority) {

        pastorQueueService.updatePriority(id, priority);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Priority updated successfully")
                .build());
    }

    @PutMapping("/queue/{id}/close")
    public ResponseEntity<ApiResponse<Void>> closeQueue(@PathVariable Long id) {
        pastorQueueService.closeQueue(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Queue item closed")
                .build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getPastorStats(
            HttpServletRequest request) {

        Long churchId = extractChurchId(request);
        Long adminId = extractAdminId(request);

        Long pending = pastorQueueService.countPending(churchId);
        Long assignedToMe = pastorQueueService.countAssignedToMe(adminId);

        Map<String, Long> stats = Map.of(
                "pending", pending,
                "assignedToMe", assignedToMe
        );

        return ResponseEntity.ok(ApiResponse.<Map<String, Long>>builder()
                .success(true)
                .data(stats)
                .build());
    }

    private Long extractChurchId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractChurchId(token);
    }

    private Long extractAdminId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtil.extractAdminId(token);
    }
}