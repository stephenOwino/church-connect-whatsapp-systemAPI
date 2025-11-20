package com.stephenotieno.church_whatsapp_system.churchconnect.service;


import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MemberRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.dto.MemberResponse;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Church;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Member;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.ChurchRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;

    @Transactional
    public MemberResponse addMember(Long churchId, MemberRequest request) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Optional<Member> existing = memberRepository.findByPhoneNumber(request.getPhoneNumber());
        if (existing.isPresent()) {
            throw new RuntimeException("Member with this phone already exists");
        }

        Member member = Member.builder()
                .church(church)
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .status("ACTIVE")
                .build();

        member = memberRepository.save(member);
        return mapToResponse(member);
    }

    public Page<MemberResponse> getAllMembers(Long churchId, Pageable pageable) {
        return memberRepository.findByChurchId(churchId, pageable)
                .map(this::mapToResponse);
    }

    public MemberResponse getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        return mapToResponse(member);
    }

    @Transactional
    public MemberResponse updateMember(Long id, MemberRequest request) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        member.setFullName(request.getFullName());
        member.setPhoneNumber(request.getPhoneNumber());

        member = memberRepository.save(member);
        return mapToResponse(member);
    }

    @Transactional
    public void deactivateMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setStatus("INACTIVE");
        memberRepository.save(member);
    }

    public Map<String, Object> getMemberStats(Long churchId) {
        Long totalMembers = memberRepository.countByChurchId(churchId);
        Long activeMembers = memberRepository.countByChurchIdAndStatus(churchId, "ACTIVE");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMembers", totalMembers);
        stats.put("activeMembers", activeMembers);
        stats.put("inactiveMembers", totalMembers - activeMembers);

        return stats;
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .phoneNumber(member.getPhoneNumber())
                .fullName(member.getFullName())
                .status(member.getStatus())
                .joinedDate(member.getJoinedDate() != null ? member.getJoinedDate().toString() : null)
                .lastActive(member.getLastActive() != null ? member.getLastActive().toString() : null)
                .build();
    }
}