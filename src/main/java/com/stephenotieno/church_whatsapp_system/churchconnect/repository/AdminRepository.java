package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    // OLD METHOD - causes lazy loading issue
    Optional<Admin> findByEmail(String email);

    // NEW METHOD - eagerly loads church with JOIN FETCH
    @Query("SELECT a FROM Admin a LEFT JOIN FETCH a.church WHERE a.email = :email")
    Optional<Admin> findByEmailWithChurch(@Param("email") String email);

    Boolean existsByEmail(String email);
}