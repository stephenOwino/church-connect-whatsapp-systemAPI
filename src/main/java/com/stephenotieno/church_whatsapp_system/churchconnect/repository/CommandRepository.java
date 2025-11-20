package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Command;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface CommandRepository extends JpaRepository<Command, Long> {

    Page<Command> findByChurchIdOrderByCreatedAtDesc(Long churchId, Pageable pageable);

    List<Command> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    List<Command> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    Page<Command> findByChurchIdAndCommandTypeOrderByCreatedAtDesc(
            Long churchId,
            String commandType,
            Pageable pageable
    );

    @Query("SELECT c.commandType, COUNT(c) FROM Command c WHERE c.church.id = :churchId GROUP BY c.commandType")
    List<Object[]> countByCommandType(@Param("churchId") Long churchId);

    @Query("SELECT c FROM Command c WHERE c.church.id = :churchId AND c.createdAt BETWEEN :startDate AND :endDate")
    List<Command> findByChurchIdAndDateRange(
            @Param("churchId") Long churchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(c) FROM Command c WHERE c.church.id = :churchId AND c.success = false")
    Long countFailedCommands(@Param("churchId") Long churchId);

    @Query("SELECT AVG(c.executionTimeMs) FROM Command c WHERE c.church.id = :churchId AND c.commandType = :commandType")
    Double averageExecutionTime(@Param("churchId") Long churchId, @Param("commandType") String commandType);

    @Query("SELECT c.commandType, COUNT(c), AVG(c.executionTimeMs) FROM Command c WHERE c.church.id = :churchId AND c.createdAt >= :since GROUP BY c.commandType ORDER BY COUNT(c) DESC")
    List<Object[]> getCommandStats(@Param("churchId") Long churchId, @Param("since") LocalDateTime since);
}