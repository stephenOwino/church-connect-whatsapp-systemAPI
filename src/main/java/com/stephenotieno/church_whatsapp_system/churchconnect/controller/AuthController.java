package com.stephenotieno.church_whatsapp_system.churchconnect.controller;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.ApiResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.AuthResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.LoginRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.RegisterRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Church registered successfully")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                    .success(true)
                    .message("Login successful")
                    .data(response)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.<AuthResponse>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }
}