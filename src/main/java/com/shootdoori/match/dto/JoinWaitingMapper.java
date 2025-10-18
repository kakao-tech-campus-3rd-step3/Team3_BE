package com.shootdoori.match.dto;

import com.shootdoori.match.entity.team.join.JoinWaiting;
import org.springframework.stereotype.Component;

@Component
public class JoinWaitingMapper {
    public JoinWaitingResponseDto toJoinWaitingResponseDto(JoinWaiting joinWaiting) {
        return new JoinWaitingResponseDto(
            joinWaiting.getId(),
            joinWaiting.getApplicant().getName(),
            joinWaiting.getTeam().getTeamId(),
            joinWaiting.getTeam().getTeamName().name(),
            joinWaiting.getApplicant().getId(),
            joinWaiting.getStatus().getDisplayName(),
            joinWaiting.getDecisionReason(),
            joinWaiting.getDecidedBy() != null ? joinWaiting.getDecidedBy().getName() : null,
            joinWaiting.getDecidedAt()
        );
    }
}
