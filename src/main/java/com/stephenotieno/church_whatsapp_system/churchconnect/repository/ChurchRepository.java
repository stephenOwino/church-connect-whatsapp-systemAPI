package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Church;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ChurchRepository extends JpaRepository<Church, Long> {
    Optional<Church> findByPhone(String phone);
}