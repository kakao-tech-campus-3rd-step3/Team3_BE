package com.shootdoori.match.dto;

import com.shootdoori.match.entity.JoinWaiting;
import org.springframework.stereotype.Component;

@Component
public class JoinWaitingMapper {
    public JoinWaitingResponseDto toJoinWaitingResponseDto(JoinWaiting joinWaiting) {
        return new JoinWaitingResponseDto(
            joinWaiting.getId(),
            joinWaiting.getTeam().getTeamId(),
            joinWaiting.getApplicant().getId(),
            joinWaiting.getStatus().getDisplayName(),
            joinWaiting.getDecisionReason(),
            joinWaiting.getDecidedBy() != null ? joinWaiting.getDecidedBy().getName() : null,
            joinWaiting.getDecidedAt()
        );
    }
}
