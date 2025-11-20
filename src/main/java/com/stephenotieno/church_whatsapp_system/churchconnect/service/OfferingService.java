package com.stephenotieno.church_whatsapp_system.churchconnect.service;


import com.stephenotieno.church_whatsapp_system.churchconnect.dto.OfferingRequest;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Church;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Member;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.Offering;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.ChurchRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.MemberRepository;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.OfferingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;

    @Transactional
    public Offering recordOffering(Long churchId, OfferingRequest request) {
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Member member = null;
        if (request.getMemberId() != null) {
            member = memberRepository.findById(request.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Member not found"));
        }

        Offering offering = Offering.builder()
                .church(church)
                .member(member)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionCode(request.getTransactionCode())
                .status("COMPLETED")
                .build();

        return offeringRepository.save(offering);
    }

    public Page<Offering> getOfferings(Long churchId, Pageable pageable) {
        return offeringRepository.findByChurchId(churchId, pageable);
    }

    public Map<String, Object> getOfferingStats(Long churchId) {
        BigDecimal totalOfferings = offeringRepository.getTotalOfferingsByChurch(churchId);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        BigDecimal last30Days = offeringRepository.getOfferingsByChurchAndDateRange(churchId, thirtyDaysAgo);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOfferings", totalOfferings != null ? totalOfferings : BigDecimal.ZERO);
        stats.put("last30Days", last30Days != null ? last30Days : BigDecimal.ZERO);

        return stats;
    }

    @Transactional
    public void handleMpesaCallback(Map<String, Object> callbackData) {
        String transactionCode = (String) callbackData.get("TransactionID");
        BigDecimal amount = new BigDecimal(callbackData.get("Amount").toString());
        String phoneNumber = (String) callbackData.get("PhoneNumber");

        Optional<Member> memberOpt = memberRepository.findByPhoneNumber(phoneNumber);

        Offering offering = Offering.builder()
                .member(memberOpt.orElse(null))
                .church(memberOpt.map(Member::getChurch).orElse(null))
                .amount(amount)
                .transactionCode(transactionCode)
                .paymentMethod("MPESA")
                .status("COMPLETED")
                .build();

        offeringRepository.save(offering);
    }
}