package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByMessageSid(String messageSid);

    List<Message> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    Page<Message> findByChurchIdOrderByCreatedAtDesc(Long churchId, Pageable pageable);

    Page<Message> findByMemberIdOrderByCreatedAtDesc(Long memberId, Pageable pageable);

    Page<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId, Pageable pageable);

    List<Message> findByNeedsPastorReplyTrueAndChurchId(Long churchId);

    List<Message> findByProcessedFalseAndChurchId(Long churchId);

    @Query("SELECT m FROM Message m WHERE m.church.id = :churchId AND m.createdAt BETWEEN :startDate AND :endDate")
    List<Message> findByChurchIdAndDateRange(
            @Param("churchId") Long churchId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(m) FROM Message m WHERE m.church.id = :churchId AND m.direction = :direction")
    Long countByChurchIdAndDirection(@Param("churchId") Long churchId, @Param("direction") String direction);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.church.id = :churchId AND m.isCommand = true")
    Long countCommandsByChurchId(@Param("churchId") Long churchId);

    @Query("SELECT m FROM Message m WHERE m.church.id = :churchId AND m.phoneNumber = :phoneNumber ORDER BY m.createdAt DESC")
    List<Message> findRecentMessagesByPhoneNumber(
            @Param("churchId") Long churchId,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable
    );
}