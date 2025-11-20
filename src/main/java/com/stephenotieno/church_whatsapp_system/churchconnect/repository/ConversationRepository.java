package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByMemberId(Long memberId);

    Optional<Conversation> findByPhoneNumberAndChurchId(String phoneNumber, Long churchId);

    Page<Conversation> findByChurchIdOrderByLastMessageAtDesc(Long churchId, Pageable pageable);

    Page<Conversation> findByChurchIdAndStatusOrderByLastMessageAtDesc(
            Long churchId,
            String status,
            Pageable pageable
    );

    List<Conversation> findByMemberIdOrderByLastMessageAtDesc(Long memberId);

    @Query("SELECT c FROM Conversation c WHERE c.church.id = :churchId AND c.unreadCount > 0 ORDER BY c.lastMessageAt DESC")
    List<Conversation> findUnreadConversations(@Param("churchId") Long churchId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.church.id = :churchId AND c.status = 'ACTIVE'")
    Long countActiveConversations(@Param("churchId") Long churchId);

    @Query("SELECT c FROM Conversation c WHERE c.church.id = :churchId AND c.lastMessageAt < :dateTime AND c.status = 'ACTIVE'")
    List<Conversation> findInactiveConversations(
            @Param("churchId") Long churchId,
            @Param("dateTime") LocalDateTime dateTime
    );

    @Query("SELECT c FROM Conversation c LEFT JOIN FETCH c.member WHERE c.church.id = :churchId ORDER BY c.lastMessageAt DESC")
    Page<Conversation> findByChurchIdWithMember(@Param("churchId") Long churchId, Pageable pageable);
}