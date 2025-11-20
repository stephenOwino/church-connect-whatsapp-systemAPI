package com.stephenotieno.church_whatsapp_system.churchconnect.service;

import com.stephenotieno.church_whatsapp_system.churchconnect.dto.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.entity.*;
import com.stephenotieno.church_whatsapp_system.churchconnect.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {

    private final CommandRepository commandRepository;
    private final MemberRepository memberRepository;
    private final ChurchRepository churchRepository;

    @Transactional
    public Command logCommand(Long churchId, String phoneNumber, String commandType,
                              String commandText, String parameters,
                              boolean success, String errorMessage,
                              String responseSent, long executionTimeMs) {

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new RuntimeException("Church not found"));

        Member member = memberRepository.findByPhoneNumber(phoneNumber).orElse(null);

        Command command = Command.builder()
                .church(church)
                .member(member)
                .phoneNumber(phoneNumber)
                .commandType(commandType)
                .commandText(commandText)
                .parameters(parameters)
                .success(success)
                .errorMessage(errorMessage)
                .responseSent(responseSent)
                .executionTimeMs(executionTimeMs)
                .build();

        command = commandRepository.save(command);

        log.info("📝 Command logged: {} - {} ({}ms)", commandType, success ? "SUCCESS" : "FAILED", executionTimeMs);
        return command;
    }

    @Transactional(readOnly = true)
    public Page<CommandDTO> getAllCommands(Long churchId, Pageable pageable) {
        return commandRepository.findByChurchIdOrderByCreatedAtDesc(churchId, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<CommandDTO> getCommandsByType(Long churchId, String commandType, Pageable pageable) {
        return commandRepository.findByChurchIdAndCommandTypeOrderByCreatedAtDesc(
                        churchId, commandType, pageable)
                .map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public List<CommandDTO> getCommandsByMember(Long memberId) {
        return commandRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommandDTO> getCommandsByPhoneNumber(String phoneNumber) {
        return commandRepository.findByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommandStatsDTO getCommandStats(Long churchId) {
        List<Object[]> commandTypeCounts = commandRepository.countByCommandType(churchId);
        Long failedCommands = commandRepository.countFailedCommands(churchId);

        Map<String, Long> commandTypeMap = new HashMap<>();
        Long totalCommands = 0L;
        String mostUsedCommand = null;
        Long maxCount = 0L;

        for (Object[] row : commandTypeCounts) {
            String commandType = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            commandTypeMap.put(commandType, count);
            totalCommands += count;

            if (count > maxCount) {
                maxCount = count;
                mostUsedCommand = commandType;
            }
        }

        // Get average execution times
        Map<String, Double> avgExecutionTimes = new HashMap<>();
        for (String commandType : commandTypeMap.keySet()) {
            Double avgTime = commandRepository.averageExecutionTime(churchId, commandType);
            avgExecutionTimes.put(commandType, avgTime != null ? avgTime : 0.0);
        }

        return CommandStatsDTO.builder()
                .totalCommands(totalCommands)
                .successfulCommands(totalCommands - failedCommands)
                .failedCommands(failedCommands)
                .commandTypeCounts(commandTypeMap)
                .averageExecutionTimes(avgExecutionTimes)
                .mostUsedCommand(mostUsedCommand)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CommandUsageDTO> getCommandUsageStats(Long churchId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> stats = commandRepository.getCommandStats(churchId, since);

        Long total = stats.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        return stats.stream()
                .map(row -> {
                    String commandType = (String) row[0];
                    Long count = ((Number) row[1]).longValue();
                    Double percentage = total > 0 ? (count * 100.0 / total) : 0.0;

                    return CommandUsageDTO.builder()
                            .commandType(commandType)
                            .count(count)
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private CommandDTO mapToDTO(Command command) {
        return CommandDTO.builder()
                .id(command.getId())
                .memberId(command.getMember() != null ? command.getMember().getId() : null)
                .memberName(command.getMember() != null ? command.getMember().getFullName() : "Unknown")
                .phoneNumber(command.getPhoneNumber())
                .commandType(command.getCommandType())
                .commandText(command.getCommandText())
                .parameters(command.getParameters())
                .success(command.getSuccess())
                .errorMessage(command.getErrorMessage())
                .responseSent(command.getResponseSent())
                .executionTimeMs(command.getExecutionTimeMs())
                .createdAt(command.getCreatedAt())
                .build();
    }
}