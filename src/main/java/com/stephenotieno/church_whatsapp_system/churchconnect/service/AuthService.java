package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ChurchRepository churchRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (adminRepository.existsByEmail(request.getAdminEmail())) {
            throw new RuntimeException("Email already exists");
        }

        Church church = Church.builder()
                .name(request.getChurchName())
                .location(request.getLocation())
                .phone(request.getPhone())
                .build();
        church = churchRepository.save(church);

        Admin admin = Admin.builder()
                .church(church)
                .email(request.getAdminEmail())
                .fullName(request.getAdminName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN")
                .isActive(true)
                .build();
        admin = adminRepository.save(admin);

        // Build response WITHOUT accessing lazy-loaded church fields
        String token = jwtUtil.generateToken(admin.getEmail(), admin.getId(), church.getId());

        return AuthResponse.builder()
                .churchId(church.getId())
                .adminId(admin.getId())
                .token(token)
                .refreshToken(token)
                .adminName(admin.getFullName())
                .churchName(church.getName()) // Access church directly, not through admin
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // CHANGED: Use findByEmailWithChurch instead of findByEmail
        Admin admin = adminRepository.findByEmailWithChurch(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Now church is already loaded, no lazy loading issue
        Church church = admin.getChurch();

        String token = jwtUtil.generateToken(admin.getEmail(), admin.getId(), church.getId());

        return AuthResponse.builder()
                .churchId(church.getId())
                .adminId(admin.getId())
                .token(token)
                .refreshToken(token)
                .adminName(admin.getFullName())
                .churchName(church.getName())
                .build();
    }
}