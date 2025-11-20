package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // Keep the old method for backward compatibility
    Page<Announcement> findByChurchIdOrderByCreatedAtDesc(Long churchId, Pageable pageable);

    // NEW METHOD: Uses JOIN FETCH to eagerly load church (prevents lazy loading exception)
    @Query("SELECT a FROM Announcement a " +
            "LEFT JOIN FETCH a.church " +
            "WHERE a.church.id = :churchId " +
            "ORDER BY a.createdAt DESC")
    Page<Announcement> findByChurchIdWithChurch(@Param("churchId") Long churchId, Pageable pageable);
}

