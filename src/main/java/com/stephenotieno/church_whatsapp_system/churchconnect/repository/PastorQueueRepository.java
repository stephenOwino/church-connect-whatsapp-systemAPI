package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.PastorQueue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface PastorQueueRepository extends JpaRepository<PastorQueue, Long> {

    Page<PastorQueue> findByChurchIdOrderByCreatedAtDesc(Long churchId, Pageable pageable);

    Page<PastorQueue> findByChurchIdAndStatusOrderByCreatedAtDesc(
            Long churchId,
            String status,
            Pageable pageable
    );

    List<PastorQueue> findByChurchIdAndStatus(Long churchId, String status);

    List<PastorQueue> findByAssignedToOrderByCreatedAtDesc(Long assignedTo);

    Page<PastorQueue> findByChurchIdAndCategoryOrderByCreatedAtDesc(
            Long churchId,
            String category,
            Pageable pageable
    );

    Page<PastorQueue> findByChurchIdAndPriorityOrderByCreatedAtDesc(
            Long churchId,
            String priority,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM PastorQueue p WHERE p.church.id = :churchId AND p.status = 'PENDING'")
    Long countPendingByChurchId(@Param("churchId") Long churchId);

    @Query("SELECT p FROM PastorQueue p LEFT JOIN FETCH p.member LEFT JOIN FETCH p.message WHERE p.church.id = :churchId AND p.status = :status ORDER BY p.priority DESC, p.createdAt ASC")
    Page<PastorQueue> findByChurchIdAndStatusWithDetails(
            @Param("churchId") Long churchId,
            @Param("status") String status,
            Pageable pageable
    );

    @Query("SELECT COUNT(p) FROM PastorQueue p WHERE p.assignedTo = :adminId AND p.status = 'ASSIGNED'")
    Long countAssignedToAdmin(@Param("adminId") Long adminId);
}