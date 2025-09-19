package com.shootdoori.match.dto;

import com.shootdoori.match.entity.JoinQueue;
import org.springframework.stereotype.Component;

@Component
public class JoinQueueMapper {
    public JoinQueueResponseDto toJoinQueueResponseDto(JoinQueue joinQueue) {
        return new JoinQueueResponseDto(
            joinQueue.getId(),
            joinQueue.getTeam().getTeamId(),
            joinQueue.getApplicant().getId(),
            joinQueue.getStatus().getDisplayName(),
            joinQueue.getDecidedBy().toString(),
            joinQueue.getDecidedAt()
        );
    }
}
