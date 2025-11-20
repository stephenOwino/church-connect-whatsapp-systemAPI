package com.stephenotieno.church_whatsapp_system.churchconnect.service;


import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Admin;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new User(
                admin.getEmail(),
                admin.getPasswordHash(),
                admin.getIsActive(),
                true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + admin.getRole()))
        );
    }
}