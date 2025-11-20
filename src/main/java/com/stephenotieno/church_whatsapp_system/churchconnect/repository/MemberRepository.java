package com.stephenotieno.church_whatsapp_system.churchconnect.repository;

import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Page<Member> findByChurchId(Long churchId, Pageable pageable);
    Optional<Member> findByPhoneNumber(String phoneNumber);
    List<Member> findByChurchIdAndStatus(Long churchId, String status);
    Long countByChurchId(Long churchId);
    Long countByChurchIdAndStatus(Long churchId, String status);
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.church WHERE m.phoneNumber = :phoneNumber")
    Optional<Member> findByPhoneNumberWithChurch(@Param("phoneNumber") String phoneNumber);
}