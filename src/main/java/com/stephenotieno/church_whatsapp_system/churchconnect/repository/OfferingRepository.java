package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {
    Page<Offering> findByChurchId(Long churchId, Pageable pageable);

    @Query("SELECT SUM(o.amount) FROM Offering o WHERE o.church.id = :churchId AND o.status = 'COMPLETED'")
    BigDecimal getTotalOfferingsByChurch(Long churchId);

    @Query("SELECT SUM(o.amount) FROM Offering o WHERE o.church.id = :churchId " +
            "AND o.status = 'COMPLETED' AND o.createdAt >= :startDate")
    BigDecimal getOfferingsByChurchAndDateRange(Long churchId, LocalDateTime startDate);
}