package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.MessageAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MessageAnalyticsRepository extends JpaRepository<MessageAnalytics, Long> {

    Optional<MessageAnalytics> findByChurchIdAndDate(Long churchId, LocalDate date);

    List<MessageAnalytics> findByChurchIdAndDateBetweenOrderByDateAsc(
            Long churchId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<MessageAnalytics> findByChurchIdOrderByDateDesc(Long churchId);

    @Query("SELECT SUM(m.totalMessages) FROM MessageAnalytics m WHERE m.church.id = :churchId AND m.date BETWEEN :startDate AND :endDate")
    Long sumTotalMessages(
            @Param("churchId") Long churchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(m.newRegistrations) FROM MessageAnalytics m WHERE m.church.id = :churchId AND m.date BETWEEN :startDate AND :endDate")
    Long sumNewRegistrations(
            @Param("churchId") Long churchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT SUM(m.offeringsInitiated) FROM MessageAnalytics m WHERE m.church.id = :churchId AND m.date BETWEEN :startDate AND :endDate")
    Long sumOfferingsInitiated(
            @Param("churchId") Long churchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT m FROM MessageAnalytics m WHERE m.church.id = :churchId ORDER BY m.date DESC")
    List<MessageAnalytics> findRecentAnalytics(@Param("churchId") Long churchId, org.springframework.data.domain.Pageable pageable);
}