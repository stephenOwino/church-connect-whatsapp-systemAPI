package com.stephenotieno.church_whatsapp_system.churchconnect.controller;


import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MenuRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MenuResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<MenuResponse>> sendMenu(
            @Valid @RequestBody MenuRequest request) {

        try {
            menuService.sendCustomMenu(request.getPhoneNumber(), request);

            MenuResponse response = MenuResponse.builder()
                    .success(true)
                    .phoneNumber(request.getPhoneNumber())
                    .message("Menu sent successfully")
                    .build();

            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(true)
                    .message("Menu sent successfully")
                    .data(response)
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(false)
                    .message("Failed to send menu: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-main/{phoneNumber}")
    public ResponseEntity<ApiResponse<MenuResponse>> sendMainMenu(
            @PathVariable String phoneNumber,
            @RequestParam(required = false, defaultValue = "Member") String memberName) {

        try {
            menuService.sendMainMenu(phoneNumber, memberName);

            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(true)
                    .message("Main menu sent")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(false)
                    .message("Failed: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-offerings/{phoneNumber}")
    public ResponseEntity<ApiResponse<MenuResponse>> sendOfferingsMenu(
            @PathVariable String phoneNumber) {

        try {
            menuService.sendOfferingsMenu(phoneNumber);

            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(true)
                    .message("Offerings menu sent")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(false)
                    .message("Failed: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-info/{phoneNumber}")
    public ResponseEntity<ApiResponse<MenuResponse>> sendInfoMenu(
            @PathVariable String phoneNumber) {

        try {
            menuService.sendInfoMenu(phoneNumber);

            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(true)
                    .message("Info menu sent")
                    .build());

        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.<MenuResponse>builder()
                    .success(false)
                    .message("Failed: " + e.getMessage())
                    .build());
        }
    }
}